package com.zys.jmeter.protocol.redis.config;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


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

    private static final JedisPoolConfig CONFIG = new JedisPoolConfig();

    private static final int TIMEOUT = 60000;

    private JedisPool initJedisPool() {
        return new JedisPool(CONFIG, StringUtils.substringBefore(address, ":"), Integer.parseInt(StringUtils.substringAfter(address, ":")), TIMEOUT, password);
    }

    private JedisSentinelPool initJedisSentinelPool() {
        return new JedisSentinelPool(master, new HashSet<>(Arrays.asList(address.split(","))), CONFIG, TIMEOUT, password);
    }

    private JedisCluster initJedisCluster() {
        String[] addrs = address.split(",");
        Set<HostAndPort> nodes = new HashSet<>();
        for (String addr : addrs) {
            nodes.add(new HostAndPort(StringUtils.substringBefore(addr, ":"), Integer.parseInt(StringUtils.substringAfter(addr, ":"))));
        }
        return new JedisCluster(nodes, 2000, 2000, 3, password, new GenericObjectPoolConfig());
    }

    public void testStarted(String s) {
        testStarted();
    }

    public void testStarted() {
        if (StringUtils.isBlank(redisName)) {
            throw new IllegalArgumentException("redisName must not be empty.");
        } else {
            switch (MODE.values()[mode]) {
                case DIRECTION:
                    setProperty(new ObjectProperty(redisName, initJedisPool()));
                    break;
                case SENTINEL:
                    setProperty(new ObjectProperty(redisName, initJedisSentinelPool()));
                    break;
                case CLUSTER:
                    setProperty(new ObjectProperty(redisName, initJedisCluster()));
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
            switch (MODE.values()[mode]) {
                case DIRECTION:
                    ((JedisPool) getProperty(redisName).getObjectValue()).destroy();
                    break;
                case SENTINEL:
                    ((JedisSentinelPool) getProperty(redisName).getObjectValue()).destroy();
                    break;
                case CLUSTER:
                    ((JedisCluster) getProperty(redisName).getObjectValue()).close();
                    break;
                default:
                    throw new IllegalArgumentException("mode must not be empty.");
            }
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
