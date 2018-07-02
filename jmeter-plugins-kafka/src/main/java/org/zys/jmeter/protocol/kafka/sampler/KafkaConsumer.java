package org.zys.jmeter.protocol.kafka.sampler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.javaapi.FetchResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;
import org.apache.commons.codec.Charsets;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.kafka.config.KafkaEntity;
import org.zys.jmeter.protocol.kafka.utils.ProtostuffRuntimeUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhuyongsheng on 2018/3/22.
 */
public class KafkaConsumer extends AbstractSampler implements TestBean {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final static Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();
    private final static int FETCH_SIZE = 100000;

    private String topic;
    private int duration;
    private String wanted;


    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        StringBuffer sp = new StringBuffer("Fetch " + wanted + " in " + topic);
        res.setSamplerData(sp.toString());
        res.setSampleLabel(getName());
        res.sampleStart();
        try {
            res.setResponseData(run(), "UTF-8");
            res.setResponseCode("0");
            res.setSuccessful(true);

        } catch (Exception e) {
            e.printStackTrace();
            res.setResponseMessage(e.getMessage());
            res.setResponseCode("500");
            res.setSuccessful(false);
        } finally {
            res.sampleEnd();
            return res;
        }
    }

    private static Boolean isMatch(String msg, String wanted){
        String[] wants = wanted.split(",");
        for (String w : wants){
            if (!msg.contains(w)){
                return false;
            }
        }
        return true;
    }

    public String run() throws InterruptedException {
        KafkaEntity kafkaEntity = (KafkaEntity)getProperty(topic).getObjectValue();
        Class clazz = kafkaEntity.getSerializeClazz();
        SimpleConsumer[] simpleConsumers = kafkaEntity.getSimpleConsumers();
        JMeterVariables variables = getThreadContext().getVariables();
        Object object = variables.getObject(topic);
        if (null == object){
            object = kafkaEntity.getOffsets().clone();
            variables.putObject(topic, object);
        }
        long[] offsets = (long[])object;
        int partitionNum = kafkaEntity.getPartitionNum();
        ExecutorService executor = Executors.newFixedThreadPool(partitionNum);
        CountDownLatch latch = new CountDownLatch(partitionNum);
        StringBuilder sb = new StringBuilder();
        AtomicBoolean isCaught = new AtomicBoolean(false);
        long beginTime = System.currentTimeMillis();
        for (int partition = 0; partition < partitionNum; partition++) {
            final int a_partition = partition;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (!isCaught.get() && System.currentTimeMillis() - beginTime < duration) {
                        try {
                            FetchRequest req = new FetchRequestBuilder()
                                    .clientId(simpleConsumers[a_partition].clientId())
                                    .addFetch(topic, a_partition, offsets[a_partition], FETCH_SIZE) // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
                                    .build();
                            FetchResponse fetchResponse = simpleConsumers[a_partition].fetch(req);
                            for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(topic, a_partition)) {
                                long currentOffset = messageAndOffset.offset();
                                if (currentOffset < offsets[a_partition]) {
                                    continue;
                                }
                                offsets[a_partition] = messageAndOffset.nextOffset();
                                ByteBuffer payload = messageAndOffset.message().payload();
                                byte[] bytes = new byte[payload.limit()];
                                payload.get(bytes);
                                String msg;
                                if (null == clazz){
                                    msg = new String(bytes, Charsets.UTF_8);
                                }else {
                                    msg = GSON.toJson(ProtostuffRuntimeUtil.deserialize(bytes, clazz));
                                }
                                if (isMatch(msg,wanted)) {
                                    isCaught.set(true);
                                    sb.append("\"").append(a_partition + "_" + String.valueOf(currentOffset)).append("\":").append(msg).append("\n");
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdownNow();
        if (sb.length() > 0) {
            return sb.deleteCharAt(sb.lastIndexOf("\n")).toString();
        }
        return "message not found.";
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getWanted() {
        return wanted;
    }

    public void setWanted(String wanted) {
        this.wanted = wanted;
    }

}
