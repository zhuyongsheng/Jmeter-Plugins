package org.zys.jmeter.protocol.kafka.config;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

/**
 * Created by 01369755 on 2018/3/22.
 */
public class KafkaConfigBeanInfo extends BeanInfoSupport {

    public KafkaConfigBeanInfo ()

    {
        super(KafkaConfig.class);
        createPropertyGroup("Kafka信息", new String[] { "topicName", "brokers", "partitionNum", "serializer", "clazz", "role"});
        PropertyDescriptor p = property("topicName");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("brokers");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("partitionNum");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, 4);
        p = property("serializer");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "STRING");
        p.setValue(TAGS, new String[]{"STRING", "PROTOSTUFF"});
        p.setValue(NOT_OTHER, Boolean.TRUE);
        p = property("clazz");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("role", KafkaConfig.ROLES.class);
        p.setValue(DEFAULT, KafkaConfig.ROLES.PRODUCER);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);

        /*
        p = property("role");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "PRODUCER");
        p.setValue(TAGS, KafkaConfig.roles);
        p.setValue(NOT_OTHER, Boolean.TRUE);*/
    }
}
