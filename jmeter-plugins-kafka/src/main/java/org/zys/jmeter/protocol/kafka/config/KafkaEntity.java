package org.zys.jmeter.protocol.kafka.config;

import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.producer.Producer;

/**
 * Created by 01369755 on 2018/6/20.
 */
public class KafkaEntity {

    private Producer producer;
    private SimpleConsumer[] simpleConsumers;
    private long[] offsets;
    private Class serializeClazz;
    private int partitionNum;

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

    public void setSerializeClazz(Class serializeClazz) {
        this.serializeClazz = serializeClazz;
    }

    public int getPartitionNum() {
        return partitionNum;
    }

    public void setPartitionNum(int partitionNum) {
        this.partitionNum = partitionNum;
    }


}
