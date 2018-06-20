package org.zys.jmeter.protocol.kafka.config;


import kafka.javaapi.consumer.SimpleConsumer;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.kafka.utils.KafkaUtil;

/**
 * Created by zhuyongsheng on 2018/3/22.
 */
public class KafkaConfig extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    private String topicName;
    private String brokers;
    private int partitionNum;
    private int role;
    private String serializer;
    private String clazz;

    @Override
    public void testStarted() {
        KafkaEntity kafkaEntity = new KafkaEntity();

        switch (serializer) {
            case "STRING":
                kafkaEntity.setSerializeClazz(null);
                break;
            case "PROTOSTUFF":
                if (JOrphanUtils.isBlank(clazz)) {
                    throw new IllegalArgumentException("Class Name must not be empty for element: " + getName() + " while serializer is PROTOSTUFF!");
                } else {
                    try {
                        kafkaEntity.setSerializeClazz(Class.forName(clazz));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        if ((ROLES.PRODUCER.ordinal() == role)) {
            kafkaEntity.setProducer(KafkaUtil.initProducer(brokers));
        }
        if ((ROLES.CONSUMER.ordinal() == role)) {
            kafkaEntity.setPartitionNum(partitionNum);
            SimpleConsumer[] simpleConsumers = KafkaUtil.initConsumer(partitionNum, brokers, topicName);
            kafkaEntity.setSimpleConsumers(simpleConsumers);
            kafkaEntity.setOffsets(KafkaUtil.initOffset(simpleConsumers, topicName, partitionNum));
        }
        setProperty(new ObjectProperty(topicName, kafkaEntity));
    }

    @Override
    public void testStarted(String s) {
        testStarted();
    }

    @Override
    public void testEnded() {
        if ((ROLES.PRODUCER.ordinal() == role)) {
            ((KafkaEntity) getProperty(topicName).getObjectValue()).getProducer().close();
        }
        if ((ROLES.CONSUMER.ordinal() == role)) {
            for (int partition = 0; partition < partitionNum; partition++) {
                ((KafkaEntity) getProperty(topicName).getObjectValue()).getSimpleConsumers()[partition].close();
            }
        }

    }

    @Override
    public void testEnded(String s) {
        testEnded();
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

    public enum ROLES {
        PRODUCER,
        CONSUMER
    }
}
