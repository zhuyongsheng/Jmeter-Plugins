package org.zys.jmeter.protocol.rpc.sampler.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

/**
 * Created by 01369755 on 2018/3/8.
 */
public class ObjectMapperUtil {

    public static final ObjectMapper objectMapper;
    static {
        objectMapper = new ObjectMapper();
         /*序列化时使用*/
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);// 禁用空对象转换json校验
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);// 去掉默认的时间戳格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));// 序列化时，日期的统一格式
         /*反序列化时使用*/
        objectMapper.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);// 反序列化时，属性不存在的兼容处理
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);// 忽略未知的字段
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);// 单引号处理
    }
}
