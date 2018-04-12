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

    private final static String PRODUCER = "PRODUCER_";
    private final static String CONSUMER = "CONSUMER_";
    private final static String OFFSET = "OFFSET_";

    private static ConcurrentHashMap<String, Object> KAFKA_VAR= new ConcurrentHashMap<>();

    private String topic;
    private String brokers;
    private int    partitionNum;
    private int    role;

    @Override
    public void testStarted() {
        initProducer();
        initConsumer();
    }

    @Override
    public void testStarted(String s) {
        testStarted();
    }

    @Override
    public void testEnded() {
        closeProducer();
        closeConsumer();
    }

    @Override
    public void testEnded(String s) {
        testEnded();
    }

//    public static void main(String[] args){
//        System.out.println(ROLES.PRODUCER.equals(ROLES.values()[0]));
//    }
    private void initProducer(){
        if ((ROLES.PRODUCER.ordinal() == role) || (ROLES.BOTH.ordinal() == role)){
            try{
                Properties props = new Properties();
                props.put("metadata.broker.list", brokers);
                ProducerConfig config = new ProducerConfig(props);
                Producer<String, byte[]> producer = new Producer(config);
                KAFKA_VAR.put(PRODUCER + topic, producer);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void closeProducer(){
        if ((ROLES.PRODUCER.ordinal() == role) || (ROLES.BOTH.ordinal() == role)){
            try{
                getProducer(topic).close();
                KAFKA_VAR.remove(PRODUCER + topic);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static SimpleConsumer[] getConsumer(String topic){
        return (SimpleConsumer[])KAFKA_VAR.get(CONSUMER + topic);
    }
    public static long[] getOffsets(String topic){
        return (long[])KAFKA_VAR.get(OFFSET + topic);
    }
    public static void updateOffset(String topic, long[] offsets){
        KAFKA_VAR.put(OFFSET + topic, offsets);
    }
    public static Producer getProducer(String topic){
        return (Producer)KAFKA_VAR.get(PRODUCER + topic);
    }
    private void initConsumer(){
        if ((ROLES.CONSUMER.ordinal() == role) || (ROLES.BOTH.ordinal() == role)){
            SimpleConsumer[] simpleConsumers = new SimpleConsumer[partitionNum];
            long[] offsets = new long[partitionNum];
            try{
                for (int partition = 0; partition < partitionNum; partition++) {
                    Broker b = findLeader(brokers, topic, partition);
                    String clientId = topic + "_" + partition;
                    simpleConsumers[partition] = new SimpleConsumer(b.host(), b.port(), TIME_OUT, BUFFER_SIZE, clientId);
                    offsets[partition] = getLastOffset(simpleConsumers[partition], partition);
                }
                KAFKA_VAR.put(CONSUMER + topic, simpleConsumers);
                KAFKA_VAR.put(OFFSET + topic, offsets);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void closeConsumer(){
        if ((ROLES.CONSUMER.ordinal() == role) || (ROLES.BOTH.ordinal() == role)){
            try{
                for (SimpleConsumer simpleConsumer : getConsumer(topic)) {
                    simpleConsumer.close();
                }
                KAFKA_VAR.remove(CONSUMER + topic);
                KAFKA_VAR.remove(OFFSET + topic);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private long getLastOffset(SimpleConsumer consumer, int partition) {
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
