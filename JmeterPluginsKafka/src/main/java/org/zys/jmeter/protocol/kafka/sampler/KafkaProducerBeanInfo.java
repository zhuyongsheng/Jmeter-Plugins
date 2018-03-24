package org.zys.jmeter.protocol.kafka.sampler;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;

import java.beans.PropertyDescriptor;

/**
 * Created by 01369755 on 2018/3/22.
 */
public class KafkaProducerBeanInfo extends BeanInfoSupport {
    public KafkaProducerBeanInfo ()

    {
        super(KafkaProducer.class);
        createPropertyGroup("消息信息", new String[] { "topic", "serializer", "message"});
        PropertyDescriptor p = property("topic");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("serializer",TypeEditor.ComboStringEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "STRING");
        p.setValue(TAGS, new String[]{"STRING", "FVP(PROTOSTUFF)"});
        p = property("message", TypeEditor.TextAreaEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
    }
}
