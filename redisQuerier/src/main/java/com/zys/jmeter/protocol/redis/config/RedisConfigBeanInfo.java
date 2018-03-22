﻿package com.zys.jmeter.protocol.redis.config;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;

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
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("address");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("password", TypeEditor.PasswordEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("sentinel");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.TRUE);
        p = property("master");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
    }
}
