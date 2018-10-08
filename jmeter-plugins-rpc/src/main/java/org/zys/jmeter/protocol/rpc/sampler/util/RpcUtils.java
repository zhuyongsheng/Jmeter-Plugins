package org.zys.jmeter.protocol.rpc.sampler.util;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFilter;
import org.apache.jorphan.reflect.ClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by zhuyongsheng on 2018/6/1.
 */
public class RpcUtils {

    private static final Logger log = LoggerFactory.getLogger(RpcUtils.class);
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().serializeNulls().create();
    private static final ApplicationConfig DUBBO_SAMPLER = new ApplicationConfig("dubboSampler");
    private static final Set DIRECT_SERVICE_PROTOCOL = new HashSet<String>() {{
        add("dubbo");
        add("rmi");
        add("hessian");
        add("http");
        add("webservice");
        add("thrift");
        add("memcached");
        add("redis");
        add("rest");
    }};
    private static final Set REGISTER_PROTOCOL = new HashSet<String>() {{
        add("zookeeper");
        add("multicast");
        add("redis");
        add("simple");
    }};

    private static final Map<String, Map<String, Method>> interfaceMap = new HashMap<>();

    private static final String[] SPATHS = new String[]{
            JMeterUtils.getJMeterHome() + "/lib/dubbo" //需将/lib/dubbo加入user.classpath配置中，以加载类
    };

    private RpcUtils() {
    }

    public static String invoke(String protocol, String host, int port, String clsName, String version, String group, String methodName, Collection<String> args) throws Exception {
        Object remoteObject = getRemoteObject(protocol, host, port, clsName, version, group);
        Method method = getMethod(clsName, methodName);
        Object[] objects = getArgs(method, args.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        return GSON.toJson(method.invoke(remoteObject, objects));
    }

    private static Method getMethod(String className, String methodName) {
        return interfaceMap.get(className).get(methodName);
    }

    public static String[] getMethodNames(String clsName) {
        if (null == interfaceMap.get(clsName)) {
            Map<String, Method> mMap = new HashMap<>();
            try {
                for (Method m : Class.forName(clsName).getMethods()) {
                    mMap.put(getMethodName(m), m);
                }
            } catch (ClassNotFoundException e) {
                log.error("class {} not found!", clsName);
            }
            interfaceMap.put(clsName, mMap);
        }
        return interfaceMap.get(clsName).keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
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
        if (MapUtils.isEmpty(interfaceMap)) {
            try {
                ClassFinder.findClasses(SPATHS, new InterfaceFilter())
                        .forEach(clazz -> interfaceMap.put(clazz, null));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return interfaceMap.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    private static Object[] getArgs(Method method, String[] args) {
        Class<?>[] types = method.getParameterTypes();
        Object[] objects = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            objects[i] = GSON.fromJson(args[i], types[i]);
        }
        return objects;
    }

    /**
     * JMeter插件设计时，对于中间件（如redis，HBase..）一般采用JMeterProperty，
     * 因为中间件对于一套SUT来说，信息是确定的，是全局的，并且一般只用来做信息验证，不需要区分线程；
     * 对于SUT对外接口（如RPC）则需要采用JMeterVariables，以在多线程时，能够更好的模拟多个用户。
     *
     * @return Reference
     * @author zhuyongsheng
     */
    private static Object getRemoteObject(String protocol, String host, int port, String clsName, String version, String group) throws Exception {
        String key = host + "_" + clsName + "_" + version + "_" + group;
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        Object object = variables.getObject(key);
        if (null == object) {
            ReferenceConfig ref = new ReferenceConfig();
            ref.setApplication(DUBBO_SAMPLER);
            ref.setInterface(clsName);
            ref.setVersion(version);
            ref.setGroup(group);
            if (REGISTER_PROTOCOL.contains(protocol)) {
                RegistryConfig registryConfig = new RegistryConfig();
                registryConfig.setProtocol(protocol);
                registryConfig.setAddress(host);
                registryConfig.setPort(port);
                ref.setRegistry(registryConfig);
            } else if (DIRECT_SERVICE_PROTOCOL.contains(protocol)) {
                ref.setUrl(new URL(protocol, host, port).toIdentityString());//直接指定服务地址
            } else {
                throw new Exception("unknown protocol Exception.");
            }
            variables.putObject(key, ref.get());
            return ref.get();
        }
        return object;
    }

    private static class InterfaceFilter implements ClassFilter {
        private final ClassLoader contextClassLoader
                = Thread.currentThread().getContextClassLoader();

        InterfaceFilter() {
        }

        @Override
        public boolean accept(String className) {
            try {
                return Class.forName(className, false, contextClassLoader).isInterface();
            } catch (ClassNotFoundException e) {
                log.error("class {} not found!", className);
                return false;
            }
        }
    }
}
