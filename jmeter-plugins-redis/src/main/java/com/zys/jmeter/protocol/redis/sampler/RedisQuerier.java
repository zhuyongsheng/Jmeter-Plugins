package com.zys.jmeter.protocol.redis.sampler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zys.jmeter.protocol.redis.config.RedisConfig;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * Created by zhuyongsheng on 2018/3/2.
 */
public class RedisQuerier extends AbstractSampler implements TestBean {

    private static final Logger log = LoggerFactory.getLogger(RedisQuerier.class);

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private String redis;
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
            return GSON.toJson(oi.readObject());
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
        Jedis jedis = RedisConfig.getPool(redis).getResource();
        String result = query(jedis, key);
        RedisConfig.getPool(redis).returnResource(jedis);
        return result;
    }

    public String getRedis() {
        return redis;
    }

    public void setRedis(String redis) {
        this.redis = redis;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
