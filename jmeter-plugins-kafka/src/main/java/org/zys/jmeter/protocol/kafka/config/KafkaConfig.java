package org.zys.jmeter.protocol.kafka.config;


import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuyongsheng on 2018/3/22.
 */
public class KafkaConfig extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    private String topicName;
    private String brokers;
    private boolean producerFlag;
    private boolean consumerFlag;
    private String serializer;
    private String clazz;

    @Override
    public void testStarted() {
        KafkaProperty kafkaProperty = new KafkaProperty();
        kafkaProperty.setTopic(topicName);
        try {
            kafkaProperty.setSerializeClazz("STRING".equals(serializer) ? null : Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            log.info(e.toString());
        }
        if (producerFlag) {
            kafkaProperty.initProducer(brokers);
        }
        if (consumerFlag) {
            kafkaProperty.initConsumerAndOffsets(brokers);
        }
        setProperty(new ObjectProperty(topicName, kafkaProperty));
    }

    @Override
    public void testStarted(String s) {
        testStarted();
    }

    @Override
    public void testEnded() {
        ((KafkaProperty) getProperty(topicName).getObjectValue()).destroy();
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


    public boolean getProducerFlag() {
        return producerFlag;
    }

    public void setProducerFlag(boolean producerFlag) {
        this.producerFlag = producerFlag;
    }

    public boolean getConsumerFlag() {
        return consumerFlag;
    }

    public void setConsumerFlag(boolean consumerFlag) {
        this.consumerFlag = consumerFlag;
    }


}
