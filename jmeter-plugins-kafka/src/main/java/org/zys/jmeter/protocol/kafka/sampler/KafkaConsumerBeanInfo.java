package org.zys.jmeter.protocol.kafka.sampler;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;

import java.beans.PropertyDescriptor;

/**
 * Created by zhuyongsheng on 2018/3/22.
 */
public class KafkaConsumerBeanInfo  extends BeanInfoSupport {
    public KafkaConsumerBeanInfo ()

    {
        super(KafkaConsumer.class);
        createPropertyGroup("匹配信息", new String[] { "topic", "wanted", "duration"});
        PropertyDescriptor p = property("topic");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("wanted");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("duration");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, 6000);
    }
}
