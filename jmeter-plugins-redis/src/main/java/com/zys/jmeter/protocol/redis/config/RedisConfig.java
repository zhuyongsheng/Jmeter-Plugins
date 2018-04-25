package com.zys.jmeter.protocol.redis.config;


import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jmeter.testbeans.TestBean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import redis.clients.util.Pool;

/**
 * Created by 01369755 on 2018/3/17.
 */
public class RedisConfig extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    private String redisName;
    private String address;
    private String master;
    private String password;
    private int sentinel;

    private static JedisPoolConfig CONFIG = new JedisPoolConfig();

    private static int TIMEOUT = 60000;

    private JedisPool initJedisPool(){
        String host = address.split(",")[0];
        return new JedisPool(CONFIG, host.split(":")[0], Integer.parseInt(host.split(":")[1]), TIMEOUT, password);
    }

    private JedisSentinelPool initJedisSentinelPool(){
        String[] hosts = address.split(",");
        Set<String> sentinels = new HashSet<>();
        for (String sentinel : hosts) {
            sentinels.add(sentinel);
        }
        return new JedisSentinelPool(master, sentinels, CONFIG, TIMEOUT, password);
    }
    public static Pool<Jedis> getPool(String redisName) throws Exception{
        Object object = JMeterContextService.getContext().getVariables().getObject(redisName);
        if (object == null) {
            throw new Exception("No pool found named: '" + redisName);
        }else {
            return (Pool<Jedis>)object;
        }
    }

    public void testStarted(String s) {
        testStarted();
    }

    public void testStarted() {
        JMeterVariables variables = getThreadContext().getVariables();
        if(JOrphanUtils.isBlank(redisName)) {
            throw new IllegalArgumentException("redisName must not be empty.");
        } else if (variables.getObject(redisName) != null) {
            log.error("Redis config already defined.");
        } else {
            if (SENTINEL.YES.ordinal() == sentinel){
                variables.putObject(redisName, initJedisSentinelPool());
            }else {
                variables.putObject(redisName, initJedisPool());
            }
        }
    }

    public void testEnded(String s) {
        testEnded();
    }

    public void testEnded() {
        ((Pool<Jedis>)JMeterContextService.getContext().getVariables().getObject(redisName)).destroy();
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
