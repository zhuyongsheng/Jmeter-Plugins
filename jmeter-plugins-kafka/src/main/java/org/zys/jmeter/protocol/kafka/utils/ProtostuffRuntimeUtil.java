package org.zys.jmeter.protocol.kafka.utils;

/**
 * Created by zhuyongsheng on 2018/5/28.
 */

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;

import static com.dyuproject.protostuff.runtime.RuntimeSchema.getSchema;

public class ProtostuffRuntimeUtil {
    private static final int linkedBufferSize = 512;

    public static <T> byte[] serialize(T t) {
        Schema schema = getSchema(t.getClass());
        return ProtostuffIOUtil.toByteArray(t, schema, LinkedBuffer.allocate(linkedBufferSize));
    }

    public static Object deserialize(byte[] bytes, Class clazz) {

        Object obj = null;
        try {
            obj = clazz.newInstance();
            Schema schema = getSchema(obj.getClass());
            ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}