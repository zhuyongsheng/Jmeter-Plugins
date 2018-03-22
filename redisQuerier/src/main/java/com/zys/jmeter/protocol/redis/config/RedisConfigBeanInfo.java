package com.zys.jmeter.protocol.redis.config;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

/**
 * Created by 01369755 on 2018/3/20.
 */
public class RedisConfigBeanInfo extends BeanInfoSupport {

    public RedisConfigBeanInfo ()

    {
        super(RedisConfig.class);
        createPropertyGroup("Redis信息", new String[] { "redisName", "address", "sentinel", "master", "password"});
        PropertyDescriptor p = property("redisName");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("address");
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
        p.setValue("default", "");
    }
}
