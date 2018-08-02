package com.zys.jmeter.protocol.redis.sampler;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

/**
 * Created by zhuyongsheng on 2018/3/2.
 */
public class RedisQuerierBeanInfo extends BeanInfoSupport {
    public RedisQuerierBeanInfo() {
        super(RedisQuerier.class);
//        createPropertyGroup("Redis信息", new String[] { "address", "sentinel", "master", "password"});
        createPropertyGroup("操作信息", new String[] {"redis", "opr", "key", "value"});
        PropertyDescriptor p = property("redis");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("opr", RedisQuerier.OPRS.class);
        p.setValue(DEFAULT, RedisQuerier.OPRS.READ.ordinal());
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p = property("key");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("value");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
    }
}
