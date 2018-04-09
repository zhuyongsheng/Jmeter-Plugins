package org.zys.jmeter.protocol.kafka.config;


import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.producer.ProducerConfig;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kafka.javaapi.producer.Producer;
import kafka.javaapi.consumer.SimpleConsumer;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static kafka.api.OffsetRequest.CurrentVersion;
import static kafka.api.OffsetRequest.LatestTime;

/**
 * Created by 01369755 on 2018/3/22.
 */
public class KafkaConfig  extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    private final static int TIME_OUT = 100000;
    private final static int BUFFER_SIZE = 64 * 1024;

    private String topic;
    private String brokers;
    private int    partitionNum;
    private int    role;

    public static ConcurrentHashMap<String, Producer> producerMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, SimpleConsumer[]> consumerMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long[]> offsetMap = new ConcurrentHashMap<>();



    @Override
    public void testStarted() {
        switch (ROLES.values()[role]){
            case PRODUCER :
                initProducer();
                break;
            case CONSUMER :
                initConsumer();
                getoffsets();
                break;
            case BOTH :
                initProducer();
                initConsumer();
                getoffsets();
                break;
            default:
                throw new UnsupportedOperationException("没有你要的这种角色");
        }
    }

    @Override
    public void testStarted(String s) {
        testStarted();
    }

    @Override
    public void testEnded() {

        switch (ROLES.values()[role]){
            case PRODUCER :
                closeProducer();
                break;
            case CONSUMER :
                closeConsumer();
                clearoffsets();
                break;
            case BOTH :
                closeProducer();
                closeConsumer();
                clearoffsets();
                break;
            default:
                throw new UnsupportedOperationException("没有你要的这种角色");
        }
    }

    @Override
    public void testEnded(String s) {
        testEnded();
    }

    private void initProducer(){
        try{
            Properties props = new Properties();
            props.put("metadata.broker.list", brokers);
            ProducerConfig config = new ProducerConfig(props);
            Producer<String, byte[]> producer = new Producer(config);
            producerMap.put(topic, producer);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void closeProducer(){
        try{
            producerMap.get(topic).close();
            producerMap.remove(topic);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initConsumer(){
        SimpleConsumer[] simpleConsumers = new SimpleConsumer[partitionNum];
        try{
            for (int partition = 0; partition < partitionNum; partition++) {
                Broker b = findLeader(brokers, topic, partition);
                String clientId = topic + "_" + partition;
                simpleConsumers[partition] = new SimpleConsumer(b.host(), b.port(), TIME_OUT, BUFFER_SIZE, clientId);
            }
            consumerMap.put(topic, simpleConsumers);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void closeConsumer(){
        try{
            for (int partition = 0; partition < partitionNum; partition++) {
                consumerMap.get(topic)[partition].close();
            }
            consumerMap.remove(topic);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getoffsets(){
        Long[] offsets = new Long[partitionNum];
        try{
            for (int partition = 0; partition < partitionNum; partition++) {
                offsets[partition] = getLastOffset(consumerMap.get(topic)[partition], partition);
            }
            offsetMap.put(topic, offsets);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void clearoffsets(){
        offsetMap.remove(topic);
    }

    public long getLastOffset(SimpleConsumer consumer, int partition) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(LatestTime(), 1));
        OffsetRequest request = new OffsetRequest(requestInfo, CurrentVersion(), consumer.clientId());
        OffsetResponse response = consumer.getOffsetsBefore(request);

        if (response.hasError()) {
            System.out.println("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition) );
            return 0;
        }
        long[] offsets = response.offsets(topic, partition);
        if (offsets.length > 0){
            return offsets[0];
        }
        return 0;
    }

    private static Broker findLeader(String brokers, String topic, int a_partition) {
        List<String> Brokers = new ArrayList<>(Arrays.asList(brokers.split(",")));
        PartitionMetadata returnMetaData = null;
        loop:
        for (String seed : Brokers) {
            SimpleConsumer consumer = null;
            try {
                consumer = new SimpleConsumer(seed.split(":")[0], Integer.parseInt(seed.split(":")[1]), TIME_OUT, BUFFER_SIZE, "leaderLookup");
                List<String> topics = Collections.singletonList(topic);
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                TopicMetadataResponse resp = consumer.send(req);

                List<TopicMetadata> metaData = resp.topicsMetadata();
                for (TopicMetadata item : metaData) {
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        if (part.partitionId() == a_partition) {
                            returnMetaData = part;
                            break loop;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error communicating with Broker [" + seed + "] to find Leader for [" + topic
                        + ", " + a_partition + "] Reason: " + e);
            } finally {
                if (consumer != null) consumer.close();
            }
        }
        return returnMetaData.leader();
    }


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBrokers() {
        return brokers;
    }

    public void setBrokers(String brokers) {
        this.brokers = brokers;
    }

    public int getPartitionNum() {
        return partitionNum;
    }

    public void setPartitionNum(int partitionNum) {
        this.partitionNum = partitionNum;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public enum ROLES
    {
        PRODUCER,
        CONSUMER,
        BOTH
    }
}
