package com.zys.jmeter.protocol.redis.sampler;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

/**
 * Created by 01369755 on 2018/3/2.
 */
public class RedisQuerierBeanInfo extends BeanInfoSupport {
    public RedisQuerierBeanInfo() {
        super(RedisQuerier.class);
//        createPropertyGroup("Redis信息", new String[] { "address", "sentinel", "master", "password"});
        createPropertyGroup("查询信息", new String[] {"redis", "key"});
       /* PropertyDescriptor p = property("address");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("password");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("sentinel");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", Boolean.TRUE);
        p = property("master");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");*/
        /*p = property("sentinel");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);
        p.setValue(NOT_OTHER,Boolean.TRUE);
        p.setValue(TAGS,new Boolean[]{
                Boolean.FALSE,
                Boolean.TRUE
        });*/
        PropertyDescriptor p = property("redis");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("key");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
    }
}
