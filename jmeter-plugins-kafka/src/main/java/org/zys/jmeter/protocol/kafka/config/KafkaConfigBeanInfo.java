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
        createPropertyGroup("Kafka信息", new String[] { "topic", "brokers", "partitionNum", "role"});
        PropertyDescriptor p = property("topic");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("brokers");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("partitionNum");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, 4);
        p = property("role");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "PRODUCER");
        p.setValue(TAGS, new String[]{"PRODUCER", "CONSUMER", "BOTH"});
        p.setValue(NOT_OTHER, Boolean.TRUE);
    }
}
