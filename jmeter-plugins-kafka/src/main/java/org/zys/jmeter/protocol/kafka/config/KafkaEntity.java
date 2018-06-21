package org.zys.jmeter.protocol.kafka.config;

import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.producer.Producer;
import org.zys.jmeter.protocol.kafka.utils.KafkaUtil;

/**
 * Created by zhuyongsheng on 2018/6/20.
 */
public class KafkaEntity {

    private Producer producer;
    private SimpleConsumer[] simpleConsumers;
    private long[] offsets;
    private Class serializeClazz;
    private int partitionNum;

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
            setPartitionNum(partitionNum);
            SimpleConsumer[] simpleConsumers = KafkaUtil.initConsumer(partitionNum, brokers, topic);
            setSimpleConsumers(simpleConsumers);
            setOffsets(KafkaUtil.initOffset(simpleConsumers, topic, partitionNum));
        }
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public SimpleConsumer[] getSimpleConsumers() {
        return simpleConsumers;
    }

    public void setSimpleConsumers(SimpleConsumer[] simpleConsumers) {
        this.simpleConsumers = simpleConsumers;
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

    public int getPartitionNum() {
        return partitionNum;
    }

    public void setPartitionNum(int partitionNum) {
        this.partitionNum = partitionNum;
    }

    public void destroy(int role){
        if (ROLES.PRODUCER.ordinal() == role) {
            producer.close();
        }
        if (ROLES.CONSUMER.ordinal() == role){
            for (SimpleConsumer simpleConsumer : simpleConsumers){
                simpleConsumer.close();
            }
        }
    }

    public enum ROLES {
        PRODUCER,
        CONSUMER
    }
}
