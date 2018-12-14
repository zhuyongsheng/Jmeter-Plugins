package com.zys.jmeter.protocol.redis.config;


import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by zhuyongsheng on 2018/3/17.
 */
public class RedisConfig extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    private String redisName;
    private String address;
    private String master;
    private String password;
    private int sentinel;

    private static final JedisPoolConfig CONFIG = new JedisPoolConfig();

    private static final int TIMEOUT = 60000;

    private JedisPool initJedisPool(){
        return new JedisPool(CONFIG, StringUtils.substringBefore(address,":"), Integer.parseInt(StringUtils.substringAfter(address,":")), TIMEOUT, password);
    }

    private JedisSentinelPool initJedisSentinelPool(){
        return new JedisSentinelPool(master, new HashSet<>(Arrays.asList(address.split(","))), CONFIG, TIMEOUT, password);
    }

    public void testStarted(String s) {
        testStarted();
    }

    public void testStarted() {
        if(StringUtils.isBlank(redisName)) {
            throw new IllegalArgumentException("redisName must not be empty.");
        } else {
            if (SENTINEL.YES.ordinal() == sentinel){
                setProperty(new ObjectProperty(redisName, initJedisSentinelPool()));
            }
            if (SENTINEL.NO.ordinal() == sentinel){
                setProperty(new ObjectProperty(redisName, initJedisPool()));
            }
        }
    }

    public void testEnded(String s) {
        testEnded();
    }


    @SuppressWarnings("unchecked")
    public void testEnded() {
        try {
            ((Pool<Jedis>)getProperty(redisName).getObjectValue()).destroy();
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

    public int getSentinel() {
        return sentinel;
    }

    public void setSentinel(int sentinel) {
        this.sentinel = sentinel;
    }

    public enum SENTINEL
    {
        YES,
        NO
    }

}
