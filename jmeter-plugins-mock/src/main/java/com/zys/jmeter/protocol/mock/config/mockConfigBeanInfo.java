package com.zys.jmeter.protocol.mock.config;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

/**
 * Created by zhuyongsheng on 2018/7/6.
 */
public class mockConfigBeanInfo extends BeanInfoSupport {
    public mockConfigBeanInfo()
    {
        super(mockConfig.class);
        createPropertyGroup("mock信息", new String[] { "serverName", "host", "port"});
        PropertyDescriptor p = property("serverName");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("host");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("port");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", 8888);
    }
}