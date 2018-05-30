package com.zys.jmeter.protocol.hbase.config;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

/**
 * Created by zhuyongsheng on 2018/4/25.
 */
public class HbaseConfigBeanInfo  extends BeanInfoSupport {
    public HbaseConfigBeanInfo()
    {
        super(HbaseConfig.class);
        createPropertyGroup("Hbase信息", new String[] { "hbaseName", "zkAddr"});
        PropertyDescriptor p = property("hbaseName");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("zkAddr");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
    }
}
