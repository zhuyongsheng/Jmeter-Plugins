package com.zys.jmeter.protocol.mock.sampler;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;

import java.beans.PropertyDescriptor;

/**
 * Created by zhuyongsheng on 2018/7/6.
 */
public class mockSamplerBeanInfo extends BeanInfoSupport {
    public mockSamplerBeanInfo()
    {
        super(mockSampler.class);
        createPropertyGroup("mock消息", new String[] { "server", "timeout", "request", "response"});
        PropertyDescriptor p = property("server");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("timeout");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", 6000);
        p = property("request", TypeEditor.TextAreaEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setValue(TEXT_LANGUAGE, "java");
        p = property("response", TypeEditor.TextAreaEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setValue(TEXT_LANGUAGE, "java");
    }
}