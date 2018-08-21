package org.zys.jmeter.protocol.kafka.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.message.MessageSet;
import kafka.javaapi.producer.Producer;
import kafka.message.MessageAndOffset;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.zys.jmeter.protocol.kafka.utils.ProtostuffRuntimeUtil;

import java.nio.ByteBuffer;
import java.util.*;

import static kafka.api.OffsetRequest.CurrentVersion;
import static kafka.api.OffsetRequest.LatestTime;

/**
 * Created by zhuyongsheng on 2018/6/20.
 */
public class KafkaProperty {

    private final static int TIME_OUT = 100000;
    private final static int BUFFER_SIZE = 64 * 1024;
    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private String topic;
    private Class serializeClazz;
    private Producer producer;
    private List<SimpleConsumer> simpleConsumerList;
    private long[] originalOffsets;

    KafkaProperty() {
    }

    @SuppressWarnings("unchecked")
    public void produce(String key, String message) {
        message = message.trim().replace("\n", "").replace("\t", "");
        producer.send(null == serializeClazz ? new KeyedMessage(topic, key.getBytes(Charsets.UTF_8), message.getBytes(Charsets.UTF_8))
                : new KeyedMessage(topic, key.getBytes(Charsets.UTF_8), ProtostuffRuntimeUtil.serialize(GSON.fromJson(message, serializeClazz))));
    }

    /**
     * 从分区0开始，依次fetch消息，若得到的消息匹配预期则返回，若不匹配则跳到下一分区，直至超时
     *
     * @param wanted   预期消息
     * @param duration 超时时间，单位毫秒
     * @return 预期的消息或者"message not found."
     * @author zhuyongsheng
     * @date 2018/8/18
     */
    public String consume(String wanted, int duration) throws InterruptedException {
        long[] offsets = getLocalOffsets();
        long beginTime = System.currentTimeMillis();
        int partition = 0;
        while (System.currentTimeMillis() - beginTime < duration) {
            MessageSet messageAndOffsets = simpleConsumerList.get(partition).fetch(
                    new FetchRequestBuilder().addFetch(topic, partition, offsets[partition], BUFFER_SIZE).build()
            ).messageSet(topic, partition);
            for (MessageAndOffset messageAndOffset : messageAndOffsets) {
                offsets[partition] = messageAndOffset.nextOffset();
                String msg = convertMessageToString(messageAndOffset);
                if (isMessageMatchWanted(msg, wanted)) {
                    return msg;
                }
            }
            partition = ++partition % simpleConsumerList.size();//如果没有消费到对应的消息，跳转到下一个分区
        }
        return "message not found.";
    }

    void destroy() {
        if (null != producer) {
            producer.close();
        }
        if (null != simpleConsumerList) {
            simpleConsumerList.forEach(SimpleConsumer::close);
        }
    }

    void initProducer(String brokers) {
        Properties props = new Properties();
        props.put("metadata.broker.list", brokers);
        this.producer = new Producer(new ProducerConfig(props));
    }

    void setSerializeClazz(Class serializeClazz) {
        this.serializeClazz = serializeClazz;
    }

    void setTopic(String topic) {
        this.topic = topic;
    }

    void initConsumerAndOffsets(String brokers, int partitionNum) {
        simpleConsumerList = new ArrayList<>();
        originalOffsets = new long[partitionNum];
        for (int p = 0; p < partitionNum; p++) {
            Broker b = findLeader(brokers, topic, p);
            if (null != b){
                SimpleConsumer s = new SimpleConsumer(b.host(), b.port(), TIME_OUT, BUFFER_SIZE, String.valueOf(p));
                simpleConsumerList.add(s);
                originalOffsets[p] = getLastOffset(s, topic, p);
            }
        }
    }

    private String convertMessageToString(MessageAndOffset messageAndOffset) {
        ByteBuffer payload = messageAndOffset.message().payload();
        byte[] bytes = new byte[payload.limit()];
        payload.get(bytes);
        return null == serializeClazz ? new String(bytes, Charsets.UTF_8)
                : GSON.toJson(ProtostuffRuntimeUtil.deserialize(bytes, serializeClazz));
    }

    /**
     * 多线程时，每个线程必须独立维护自己的消费偏移量，此方法用于获取当前线程的kafka偏移量
     *
     * @return 当前线程的kafka偏移量数组
     * @author zhuyongsheng
     * @date 2018/8/18
     */
    private long[] getLocalOffsets() {
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        Object object = variables.getObject(topic);
        if (null == object) {
            object = originalOffsets.clone();
            variables.putObject(topic, object);
        }
        return (long[]) object;
    }

    private Broker findLeader(String brokers, String topic, int a_partition) {
        PartitionMetadata metaData = null;
        loop:
        for (String seed : new ArrayList<>(Arrays.asList(brokers.split(",")))) {
            SimpleConsumer consumer = null;
            try {
                consumer = new SimpleConsumer(StringUtils.substringBefore(seed, ":"),
                        Integer.valueOf(StringUtils.substringAfter(seed, ":")), TIME_OUT, BUFFER_SIZE, "findLeader");
                for (TopicMetadata item : consumer.send(new TopicMetadataRequest(Collections.singletonList(topic))).topicsMetadata()) {
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        if (part.partitionId() == a_partition) {
                            metaData = part;
                            break loop;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (consumer != null) consumer.close();
            }
        }
        return metaData != null ? metaData.leader() : null;
    }

    private long getLastOffset(SimpleConsumer consumer, String topicName, int partition) {
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<>();
        requestInfo.put(new TopicAndPartition(topicName, partition), new PartitionOffsetRequestInfo(LatestTime(), 1));
        OffsetResponse response = consumer.getOffsetsBefore(new OffsetRequest(requestInfo, CurrentVersion(), consumer.clientId()));
        long[] offsets = response.offsets(topicName, partition);
        if (offsets.length > 0) {
            return offsets[0];
        }
        return 0;
    }

    private Boolean isMessageMatchWanted(String msg, String wanted) {
        String[] wants = wanted.split(",");
        for (String w : wants) {
            if (!msg.contains(w)) {
                return false;
            }
        }
        return true;
    }


}
