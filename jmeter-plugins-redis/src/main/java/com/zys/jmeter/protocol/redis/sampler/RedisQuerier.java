package com.zys.jmeter.protocol.redis.sampler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * Created by zhuyongsheng on 2018/3/2.
 */
public class RedisQuerier extends AbstractSampler implements TestBean {

    private static final Logger log = LoggerFactory.getLogger(RedisQuerier.class);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private String redis;
    private int opr;
    private String key;
    private String value;

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        res.setSamplerData(OPRS.values()[opr] + " " + key + (StringUtils.isNotEmpty(value) ? " with value of " + value : "."));
        res.setSampleLabel(getName());
        try {
            res.sampleStart();
            res.setResponseData(run(), "UTF-8");
            res.setResponseCode("0");
            res.setSuccessful(true);

        } catch (Exception e) {
            res.setResponseMessage(e.toString());
            res.setResponseData(e.getMessage(), "UTF-8");
            res.setResponseCode("500");
            res.setSuccessful(false);
        } finally {
            res.sampleEnd();
        }
        return res;
    }


    private String get(Jedis jedis, String key) {
        byte[] bytes = null;
        try {
            bytes = jedis.get(key.getBytes());
            ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return GSON.toJson(oi.readObject());
        } catch (Exception e) {
            if (bytes != null) {
                return new String(bytes, Charsets.UTF_8);
            }
        }
        return null;
    }

    private String read(Jedis jedis, String key) {
        StringBuilder sb = new StringBuilder();
        jedis.keys(key).forEach(k-> sb.append(k).append(" : ").append(get(jedis, k)).append("\n"));
        if (sb.length() > 0){
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
        return "no key found.";
    }

    @SuppressWarnings("unchecked")
    private String run() throws Exception {
        if (StringUtils.isEmpty(key)){
            return "key must not be empty.";
        }
        Pool<Jedis> jedisPool = (Pool<Jedis>) getProperty(redis).getObjectValue();
        Jedis jedis = jedisPool.getResource();
        String result;
        switch (OPRS.values()[opr]){
            case CREATE :
                if (StringUtils.isEmpty(value)){
                    result = "value must not be empty.";
                    break;
                }
                result = jedis.setnx(key, value).toString();
                break;
            case READ:
                result = read(jedis, key);
                break;
            case UPDATE:
                if (StringUtils.isEmpty(value)){
                    result = "value must not be empty.";
                    break;
                }
                result = jedis.set(key, value);
                break;
            case DELETE:
                result = jedis.del(key).toString();
                break;
            case TTL:
                result = jedis.ttl(key).toString();
                break;
            default:
                throw new Exception("unknown operation Exception.");
        }
        jedisPool.returnResource(jedis);
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getOpr() {
        return opr;
    }

    public void setOpr(int opr) {
        this.opr = opr;
    }


    public enum OPRS {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        TTL
    }

}
