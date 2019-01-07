package com.zys.jmeter.protocol.redis.config;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;

import java.beans.PropertyDescriptor;

/**
 * Created by zhuyongsheng on 2018/3/20.
 */
public class RedisConfigBeanInfo extends BeanInfoSupport {

    public RedisConfigBeanInfo ()

    {
        super(RedisConfig.class);
        createPropertyGroup("Redis信息", new String[] { "redisName", "mode", "address", "master", "password"});
        PropertyDescriptor p = property("redisName");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("address");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("password", TypeEditor.PasswordEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("mode", RedisConfig.MODE.class);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, RedisConfig.MODE.SENTINEL);
        p = property("master");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
    }
}
