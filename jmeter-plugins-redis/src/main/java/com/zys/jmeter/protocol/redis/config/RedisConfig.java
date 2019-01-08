package com.zys.jmeter.protocol.redis.config;


import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by zhuyongsheng on 2018/3/17.
 */
public class RedisConfig extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    private String redisName;
    private String address;
    private String master;
    private String password;
    private int mode;

    public void testStarted(String s) {
        testStarted();
    }

    public void testStarted() {
        if (StringUtils.isBlank(redisName)) {
            throw new IllegalArgumentException("redisName must not be empty.");
        } else {
            switch (MODE.values()[mode]) {
                case DIRECTION:
                    setProperty(new ObjectProperty(redisName, new RedisProperty(address, password)));
                    break;
                case SENTINEL:
                    setProperty(new ObjectProperty(redisName, new RedisProperty(master, address.split(","), password)));
                    break;
                case CLUSTER:
                    setProperty(new ObjectProperty(redisName, new RedisProperty(address.split(","), password)));
                    break;
                default:
                    throw new IllegalArgumentException("mode must not be empty.");
            }
        }
    }

    public void testEnded(String s) {
        testEnded();
    }


    public void testEnded() {
        try {
            ((RedisProperty) getProperty(redisName).getObjectValue()).destroy();
            removeProperty(redisName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRedisName() {
        return redisName;
    }

    public void setRedisName(String redisName) {
        this.redisName = redisName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }


    public enum MODE {
        DIRECTION,
        SENTINEL,
        CLUSTER
    }

}
