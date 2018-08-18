package org.zys.jmeter.protocol.kafka.config;

import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.producer.Producer;
import org.apache.commons.collections.CollectionUtils;
import org.zys.jmeter.protocol.kafka.utils.KafkaUtil;

import java.util.List;

/**
 * Created by zhuyongsheng on 2018/6/20.
 */
public class KafkaProperty {

    private Class serializeClazz;
    private Producer producer;
    private List<SimpleConsumer> simpleConsumerList;
    private long[] offsets;

    public KafkaProperty(boolean producerFlag, boolean consumerFlag, String serializer, String clazz, String brokers, String topic, int partitionNum) {
        try {
            setSerializeClazz(serializer, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (producerFlag){
            this.producer = KafkaUtil.initProducer(brokers);
        }
        if (consumerFlag){
            this.simpleConsumerList = KafkaUtil.initConsumer(partitionNum, brokers, topic);
            this.offsets = KafkaUtil.initOffset(simpleConsumerList, topic);
        }
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

    public void destroy(){
        if (null != producer) {
            producer.close();
        }
        if (CollectionUtils.isNotEmpty(simpleConsumerList)){
            simpleConsumerList.forEach(simpleConsumer -> {simpleConsumer.close();});
        }
    }

    public List<SimpleConsumer> getSimpleConsumerList() {
        return simpleConsumerList;
    }

    public Producer getProducer() {
        return producer;
    }

    public long[] getOffsets() {
        return offsets;
    }

    public Class getSerializeClazz() {
        return serializeClazz;
    }

}
