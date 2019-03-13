package org.zys.jmeter.protocol.thrift.sampler.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFilter;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by zhuyongsheng on 2018/6/1.
 */
public class ThriftUtils {

    private static final Logger log = LoggerFactory.getLogger(ThriftUtils.class);
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().serializeNulls().create();


    private static final Map<String, Map<String, Method>> serviceMap = new HashMap<>();

    private static final String[] SPATHS = new String[]{
            JMeterUtils.getJMeterHome() + "/lib/thrift" //需将/lib/dubbo加入user.classpath配置中，以加载类
    };
    private static final String[] EMPTY_METHOD = new String[]{StringUtils.EMPTY};

    private ThriftUtils() {
    }

    public static String invoke(String host, int port, String clsName, String methodName, Collection<String> args) throws Exception {
        Object remoteObject = getClient(host, port, clsName);
        Method method = getMethod(clsName, methodName);
        Object[] objects = getArgs(method, args.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        return GSON.toJson(method.invoke(remoteObject, objects));
    }

    private static Method getMethod(String className, String methodName) {
        return serviceMap.get(className).get(methodName);
    }

    public static String[] getMethodNames(String clsName) {
        if (clsName.equals(StringUtils.EMPTY)) {
            return EMPTY_METHOD;
        }
        if (null == serviceMap.get(clsName)) {
            Map<String, Method> mMap = new HashMap<>();
            try {
                for (Method m : Class.forName(clsName + "$Iface").getDeclaredMethods()) {
                    mMap.put(getMethodName(m), m);
                }
            } catch (ClassNotFoundException e) {
                log.error("class {} not found!", clsName);
            }
            serviceMap.put(clsName, mMap);
        }
        return serviceMap.get(clsName).keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    private static String getMethodName(Method method) {
        StringBuilder methodName = new StringBuilder(method.getName()).append('(');
        Class[] pts = method.getParameterTypes();
        for (int i = 0; i < pts.length; i++) {
            methodName.append(pts[i].getSimpleName());
            if (i < pts.length - 1) {
                methodName.append(',');
            }
        }
        return methodName.append(')').toString();
    }

    public static String[] getClassNames() {
        if (MapUtils.isEmpty(serviceMap)) {
            try {
                serviceMap.put(StringUtils.EMPTY, null);
                ClassFinder.findClasses(SPATHS, new ThriftServiceFilter())
                        .forEach(clazz -> serviceMap.put(clazz, null));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return serviceMap.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    private static Object[] getArgs(Method method, String[] args) {
        Class<?>[] types = method.getParameterTypes();
        Object[] objects = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            objects[i] = GSON.fromJson(args[i], types[i]);
        }
        return objects;
    }

    private static Object getClient(String host, int port, String clsName) throws Exception {
        String key = host + "_" + port + "_" + clsName;
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        Object object = variables.getObject(key);
        if (null == object) {
            TTransport tTransport = new TSocket(host, port);
            //协议要和服务端一致
            TProtocol protocol = new TBinaryProtocol(tTransport);
            tTransport.open();
            Class clientClass = Class.forName(clsName + "$Client");
            Constructor<?> cons = clientClass.getConstructor(TProtocol.class);
            Object client = cons.newInstance(protocol);
            variables.putObject(key, client);
            return client;
        }
        return object;
    }

    private static class ThriftServiceFilter implements ClassFilter {

        ThriftServiceFilter() {
        }

        @Override
        public boolean accept(String className) {
            return className.contains("Service") && !className.contains("$");
        }
    }
}
