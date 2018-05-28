package org.zys.jmeter.protocol.kafka.utils;

/**
 * Created by 01369755 on 2018/5/28.
 */

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.dyuproject.protostuff.runtime.RuntimeSchema.getSchema;

public class ProtostuffRuntimeUtil {
    private static final int linkedBufferSize = 512;

    public static <T> byte[] serialize(T o) {
        Schema schema = getSchema(o.getClass());
        return ProtostuffIOUtil.toByteArray(o, schema, LinkedBuffer.allocate(linkedBufferSize));
    }

    public static Object deserialize(byte[] bytes, Class clazz) {

        Object obj = null;
        try {
            obj = clazz.newInstance();
            Schema schema = getSchema(obj.getClass());
            ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return obj;
    }
}