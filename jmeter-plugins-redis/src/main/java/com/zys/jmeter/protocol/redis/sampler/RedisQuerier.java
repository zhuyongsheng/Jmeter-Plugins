package com.zys.jmeter.protocol.redis.sampler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.zys.jmeter.protocol.redis.config.RedisConfig;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Set;

/**
 * Created by 01369755 on 2018/3/2.
 */
public class RedisQuerier extends AbstractSampler implements TestBean {

    private static final Logger log = LoggerFactory.getLogger(RedisQuerier.class);

    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String redisName;
    private String key;

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        res.setSamplerData("get value of " + key);
        res.setSampleLabel(getName());
        try {
            res.sampleStart();
            res.setResponseData(run(),"UTF-8");
            res.setResponseCode("0");
            res.setSuccessful(true);

        } catch (Exception e) {
            res.setResponseMessage(e.toString());
            res.setResponseCode("500");
            res.setSuccessful(false);
        } finally {
            res.sampleEnd();
        }
        return res;
    }



    public String get(Jedis jedis, String key){
        byte[] bytes = null;
        try {
            bytes = jedis.get(key.getBytes());
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi;
            oi = new ObjectInputStream(bi);
            return OBJECT_MAPPER.writeValueAsString(oi.readObject());
//            return new String(jedis.get(key.getBytes()), "UTF-8");
        } catch (Exception e) {
            if(bytes != null){
                try {
                    return new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }
    public String query (Jedis jedis, String key) {
        StringBuilder sb = new StringBuilder();
        Set<String> keys = jedis.keys(key);
        if (keys.size() == 0){
            return "no key found.";
        }
        for (String  k : keys) {
            sb.append(k).append(" : ").append(get(jedis, k)).append("\n");
        }
        return sb.deleteCharAt(sb.length()-1).toString();
    }

    public String run() throws Exception {
        Jedis jedis = RedisConfig.getPool(redisName).getResource();
        String result = query(jedis, key);
        RedisConfig.getPool(redisName).returnResource(jedis);
        return result;
    }

    public String getRedisName() {
        return redisName;
    }

    public void setRedisName(String redisName) {
        this.redisName = redisName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
