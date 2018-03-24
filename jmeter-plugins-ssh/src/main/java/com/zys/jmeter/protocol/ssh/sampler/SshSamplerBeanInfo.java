package com.zys.jmeter.protocol.ssh.sampler;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;

import java.beans.PropertyDescriptor;

/**
 * Created by 01369755 on 2018/3/24.
 */
public class SshSamplerBeanInfo extends BeanInfoSupport {

    public SshSamplerBeanInfo ()

    {
        super(SshSampler.class);
        createPropertyGroup("命令信息", new String[] { "host", "cmd"});
        PropertyDescriptor p = property("host");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("cmd");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
    }
}