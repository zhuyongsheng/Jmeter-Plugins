package org.zys.jmeter.protocol.kafka.config;

import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.producer.Producer;
import org.zys.jmeter.protocol.kafka.utils.KafkaUtil;

import java.util.List;

/**
 * Created by zhuyongsheng on 2018/6/20.
 */
public class KafkaEntity {

    private Class serializeClazz;
    private Producer producer;
    private List<SimpleConsumer> simpleConsumerList;
    private long[] offsets;

    public KafkaEntity(int role, String serializer, String clazz, String brokers, String topic, int partitionNum) {
        try {
            setSerializeClazz(serializer, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ROLES.PRODUCER.ordinal() == role){
            setProducer(KafkaUtil.initProducer(brokers));
        }
        if (ROLES.CONSUMER.ordinal() == role){
            List<SimpleConsumer> simpleConsumerList = KafkaUtil.initConsumer(partitionNum, brokers, topic);
            setSimpleConsumerList(simpleConsumerList);
            setOffsets(KafkaUtil.initOffset(simpleConsumerList, topic));
        }
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public long[] getOffsets() {
        return offsets;
    }

    public void setOffsets(long[] offsets) {
        this.offsets = offsets;
    }

    public Class getSerializeClazz() {
        return serializeClazz;
    }

    public void setSerializeClazz(String serializer, String clazz) throws Exception {
        switch (serializer) {
            case "STRING":
                serializeClazz = null;
                break;
            case "PROTOSTUFF":
                serializeClazz = Class.forName(clazz);
                break;
        }
    }

    public void destroy(int role){
        if (ROLES.PRODUCER.ordinal() == role) {
            producer.close();
        }
        if (ROLES.CONSUMER.ordinal() == role){
            simpleConsumerList.forEach(simpleConsumer -> {simpleConsumer.close();});
        }
    }

    public List<SimpleConsumer> getSimpleConsumerList() {
        return simpleConsumerList;
    }

    public void setSimpleConsumerList(List<SimpleConsumer> simpleConsumerList) {
        this.simpleConsumerList = simpleConsumerList;
    }

    public enum ROLES {
        PRODUCER,
        CONSUMER
    }
}
