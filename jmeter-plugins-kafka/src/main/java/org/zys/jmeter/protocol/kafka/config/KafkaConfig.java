package org.zys.jmeter.protocol.kafka.config;


import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static kafka.api.OffsetRequest.CurrentVersion;
import static kafka.api.OffsetRequest.LatestTime;

/**
 * Created by zhuyongsheng on 2018/3/22.
 */
public class KafkaConfig extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    private final static int TIME_OUT = 100000;
    private final static int BUFFER_SIZE = 64 * 1024;

    private final static String PRODUCER = "PRODUCER_";
    private final static String CONSUMER = "CONSUMER_";
    private final static String OFFSET = "OFFSET_";
    private final static String SERIALIZER = "SERIALIZER_";

    /*考虑到kafka配置是全局的，直接用了静态Map，比JMeterVariables更适合一些；
    JMeterVariables用于不同线程中需要区分的情况*/
    private static Map<String, Object> KAFKA_VAR= new HashMap<>();

    private String topicName;
    private String brokers;
    private int    partitionNum;
    private int    role;
    private String serializer;

    private String clazz;

    @Override
    public void testStarted() {
        initProducer();
        initConsumer();
        initSerializer();
    }

    @Override
    public void testStarted(String s) {
        testStarted();
    }

    @Override
    public void testEnded() {
        closeProducer();
        closeConsumer();
        closeSerializer();
    }

    @Override
    public void testEnded(String s) {
        testEnded();
    }

//    public static void main(String[] args){
//        System.out.println(ROLES.PRODUCER.equals(ROLES.values()[0]));
//    }
    private void initProducer(){
        if ((ROLES.PRODUCER.ordinal() == role)){
            try{
                Properties props = new Properties();
                props.put("metadata.broker.list", brokers);
                ProducerConfig config = new ProducerConfig(props);
                Producer<String, byte[]> producer = new Producer(config);
                KAFKA_VAR.put(PRODUCER + topicName, producer);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void closeProducer(){
        if ((ROLES.PRODUCER.ordinal() == role)){
            try{
                getProducer(topicName).close();
                KAFKA_VAR.remove(PRODUCER + topicName);
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
    public static String getSerializeClazz(String topic){
        return (String)KAFKA_VAR.get(SERIALIZER + topic);
    }
    private void initConsumer(){
        if ((ROLES.CONSUMER.ordinal() == role)){
            SimpleConsumer[] simpleConsumers = new SimpleConsumer[partitionNum];
            long[] offsets = new long[partitionNum];
            try{
                for (int partition = 0; partition < partitionNum; partition++) {
                    Broker b = findLeader(brokers, topicName, partition);
                    String clientId = topicName + "_" + partition;
                    simpleConsumers[partition] = new SimpleConsumer(b.host(), b.port(), TIME_OUT, BUFFER_SIZE, clientId);
                    offsets[partition] = getLastOffset(simpleConsumers[partition], partition);
                }
                KAFKA_VAR.put(CONSUMER + topicName, simpleConsumers);
                KAFKA_VAR.put(OFFSET + topicName, offsets);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void closeConsumer(){
        if ((ROLES.CONSUMER.ordinal() == role)){
            try{
                for (SimpleConsumer simpleConsumer : getConsumer(topicName)) {
                    simpleConsumer.close();
                }
                KAFKA_VAR.remove(CONSUMER + topicName);
                KAFKA_VAR.remove(OFFSET + topicName);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private long getLastOffset(SimpleConsumer consumer, int partition) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topicName, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(LatestTime(), 1));
        OffsetRequest request = new OffsetRequest(requestInfo, CurrentVersion(), consumer.clientId());
        OffsetResponse response = consumer.getOffsetsBefore(request);

        if (response.hasError()) {
            System.out.println("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topicName, partition) );
            return 0;
        }
        long[] offsets = response.offsets(topicName, partition);
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
                String[] seedInfo = seed.split(":");
                consumer = new SimpleConsumer(seedInfo[0], Integer.parseInt(seedInfo[1]), TIME_OUT, BUFFER_SIZE, "leaderLookup");
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

    private void initSerializer(){
        switch (serializer) {
            case "STRING":
                KAFKA_VAR.put(SERIALIZER + topicName, "");
                break;
            case "PROTOSTUFF":
                if (JOrphanUtils.isBlank(clazz)){
                    throw new IllegalArgumentException("Class Name must not be empty for element: " + getName() + " while serializer is PROTOSTUFF!");
                } else {
                    KAFKA_VAR.put(SERIALIZER + topicName, clazz);
                }
                break;
        }
    }
    private void closeSerializer(){
        KAFKA_VAR.remove(SERIALIZER + topicName);
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topic) {
        this.topicName = topic;
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

    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }


    public enum ROLES
    {
        PRODUCER,
        CONSUMER
    }
}
