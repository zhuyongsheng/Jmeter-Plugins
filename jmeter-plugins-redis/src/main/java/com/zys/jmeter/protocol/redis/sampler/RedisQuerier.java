package com.zys.jmeter.protocol.redis.sampler;

import com.zys.jmeter.protocol.redis.config.RedisProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuyongsheng on 2018/3/2.
 */
public class RedisQuerier extends AbstractSampler implements TestBean {

    private static final Logger log = LoggerFactory.getLogger(RedisQuerier.class);

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
            e.printStackTrace();
            res.setResponseMessage(e.toString());
            res.setResponseData(e.getMessage(), "UTF-8");
            res.setResponseCode("500");
            res.setSuccessful(false);
        } finally {
            res.sampleEnd();
        }
        return res;
    }


    private String run() throws Exception {
        if (StringUtils.isEmpty(key)) {
            return "key must not be empty.";
        }
        RedisProperty redisClient = (RedisProperty) getProperty(redis).getObjectValue();
        String result;
        switch (OPRS.values()[opr]) {
            case CREATE:
                result = redisClient.create(key, value);
                break;
            case READ:
                result = redisClient.read(key);
                break;
            case UPDATE:
                result = redisClient.update(key, value);
                break;
            case DELETE:
                result = redisClient.delete(key);
                break;
            case TTL:
                result = redisClient.ttl(key);
                break;
            default:
                result = "unknown operation Exception.";
        }
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
