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
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * Created by zhuyongsheng on 2018/3/2.
 */
public class RedisQuerier extends AbstractSampler implements TestBean {

    private static final Logger log = LoggerFactory.getLogger(RedisQuerier.class);

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

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
        jedis.keys(key).forEach(k -> sb.append(k).append(" : ").append(get(jedis, k)).append("\n"));
        if (sb.length() > 0) {
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
        return "no key found.";
    }

    @SuppressWarnings("unchecked")
    private String run() throws Exception {
        if (StringUtils.isEmpty(key)) {
            return "key must not be empty.";
        }
        Object jedisClient = getProperty(redis).getObjectValue();
        String result;
        switch (OPRS.values()[opr]) {
            case CREATE:
                if (jedisClient instanceof JedisSentinelPool) {
                    Jedis jedis = ((JedisSentinelPool) jedisClient).getResource();
                    result = jedis.setnx(key, value).toString();
                    jedis.close();
                } else if (jedisClient instanceof JedisPool) {
                    Jedis jedis = ((JedisPool) jedisClient).getResource();
                    result = jedis.setnx(key, value).toString();
                    jedis.close();
                } else if (jedisClient instanceof JedisCluster) {
                    result = ((JedisCluster) jedisClient).setnx(key, value).toString();
                } else {
                    result = "unsupported mode.";
                }
                break;
            case READ:
                if (jedisClient instanceof JedisSentinelPool) {
                    Jedis jedis = ((JedisSentinelPool) jedisClient).getResource();
                    result = read(jedis, key);
                    jedis.close();
                } else if (jedisClient instanceof JedisPool) {
                    Jedis jedis = ((JedisPool) jedisClient).getResource();
                    result = read(jedis, key);
                    jedis.close();
                } else if (jedisClient instanceof JedisCluster) {
                    if (key.contains("*")) {
                        throw new Exception("Redis Cluster does not support 'keys'.");
                    } else {
                        result = ((JedisCluster) jedisClient).get(key);
                        if (StringUtils.isEmpty(result)) {
                            result = "no key found.";
                        }
                    }
                } else {
                    result = "unsupported mode.";
                }
                break;
            case UPDATE:
                if (jedisClient instanceof JedisSentinelPool) {
                    Jedis jedis = ((JedisSentinelPool) jedisClient).getResource();
                    result = jedis.set(key, value);
                    jedis.close();
                } else if (jedisClient instanceof JedisPool) {
                    Jedis jedis = ((JedisPool) jedisClient).getResource();
                    result = jedis.set(key, value);
                    jedis.close();
                } else if (jedisClient instanceof JedisCluster) {
                    result = ((JedisCluster) jedisClient).set(key, value);
                } else {
                    result = "unsupported mode.";
                }
                break;
            case DELETE:
                if (jedisClient instanceof JedisSentinelPool) {
                    Jedis jedis = ((JedisSentinelPool) jedisClient).getResource();
                    result = jedis.del(key).toString();
                    jedis.close();
                } else if (jedisClient instanceof JedisPool) {
                    Jedis jedis = ((JedisPool) jedisClient).getResource();
                    result = jedis.del(key).toString();
                    jedis.close();
                } else if (jedisClient instanceof JedisCluster) {
                    result = ((JedisCluster) jedisClient).del(key).toString();
                } else {
                    result = "unsupported mode.";
                }
                break;
            case TTL:
                if (jedisClient instanceof JedisSentinelPool) {
                    Jedis jedis = ((JedisSentinelPool) jedisClient).getResource();
                    result = jedis.ttl(key).toString();
                    jedis.close();
                } else if (jedisClient instanceof JedisPool) {
                    Jedis jedis = ((JedisPool) jedisClient).getResource();
                    result = jedis.ttl(key).toString();
                    jedis.close();
                } else if (jedisClient instanceof JedisCluster) {
                    result = ((JedisCluster) jedisClient).ttl(key).toString();
                } else {
                    result = "unsupported mode.";
                }
                break;
            default:
                throw new Exception("unknown operation Exception.");
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
