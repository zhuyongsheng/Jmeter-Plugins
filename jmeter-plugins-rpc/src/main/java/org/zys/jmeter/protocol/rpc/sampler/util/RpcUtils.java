package org.zys.jmeter.protocol.rpc.sampler.util;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhuyongsheng on 2018/6/1.
 */
public class RpcUtils {

    private static final Logger log = LoggerFactory.getLogger(RpcUtils.class);
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().serializeNulls().create();
    private static final ApplicationConfig DUBBOSAMPLER = new ApplicationConfig("dubboSampler");
    private static final int TIMEOUT = 5000;

    private static final Map<String, Map<String, Method>> interfaceMap = new HashMap<>();

    private static final String[] SPATHS = new String[]{
            JMeterUtils.getJMeterHome() + "/lib/dubbo" //需将/lib/dubbo加入user.classpath配置中，以加载类
    };

    private RpcUtils() {
    }

    public static String invoke(String protocol, String host, String port, String clsName, String version, String group, String methodName, Collection<String> args) throws Exception {
        Object remoteObject = getRemoteObject(protocol, host, port, clsName, version, group);
        Method method = getMethod(clsName, methodName);
        Object[] objects = getArgs(method, args.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        return GSON.toJson(method.invoke(remoteObject, objects));
    }

    private static Method getMethod(String className, String methodName) {
        return interfaceMap.get(className).get(methodName);
    }

    public static String[] getMethodNames(String interfaceCls) {
        if (null == interfaceMap.get(interfaceCls)) {
            Map<String, Method> mMap = new HashMap<>();
            try {
                for (Method m : Class.forName(interfaceCls).getMethods()) {
                    StringBuilder methodName = new StringBuilder(m.getName()).append('(');
                    Class[] pts = m.getParameterTypes();
                    if (null != pts && pts.length > 0) {
                        for (Class pt : pts) {
                            methodName.append(pt.getSimpleName()).append(',');
                        }
                        methodName.deleteCharAt(methodName.lastIndexOf(","));
                    }
                    methodName.append(')');
                    mMap.put(methodName.toString(), m);
                }
            } catch (ClassNotFoundException e) {
                log.error("class {} not found!", interfaceCls);
            }
            interfaceMap.put(interfaceCls, mMap);
        }
        return interfaceMap.get(interfaceCls).keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public static String[] getClassNames() {
        if (MapUtils.isEmpty(interfaceMap)) {
            try {
                ClassFinder.findClasses(SPATHS, new InterfaceFilter("Service", "RestService"))
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
     * Jmeter插件设计时，对于中间件（如redis，Hbase..）一般采用JMeterProperty，
     * 因为中间件对于一套SUT来说，信息是确定的，是全局的，并且一般只用来做信息验证，不需要区分线程；
     * 对于SUT对外接口（如RPC）则需要采用JMeterVariables，以在多线程时，能够更好的模拟多个用户。
     *
     * @return ReferenceConfig
     * @author zhuyongsheng
     */
    private static Object getRemoteObject(String protocol, String host, String port, String clsName, String version, String group) throws Exception {
        String key = host + "_" + clsName + "_" + version + "_" + group;
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        Object object = variables.getObject(key);
        if (null == object) {
            ReferenceConfig ref = new ReferenceConfig();
            ref.setApplication(DUBBOSAMPLER);
            ref.setInterface(clsName);
            ref.setVersion(version);
            ref.setGroup(group);
            ref.setTimeout(TIMEOUT);
            if (protocol.equals("dubbo")) {
                ref.setUrl(protocol + "://" + host + ":" + port + "/" + clsName);//直接指定服务地址
            } else {
                ref.setRegistry(new RegistryConfig(protocol + "://" + host + ":" + port));//通过注册中心访问
            }
            variables.putObject(key, ref.get());
            return ref.get();
        }
        return object;
    }

    private static class InterfaceFilter implements ClassFilter {
        private final String contains; // class name should contain this string
        private final String notContains; // class name should not contain this string
        private final ClassLoader contextClassLoader
                = Thread.currentThread().getContextClassLoader();

        InterfaceFilter(String contains, String notContains) {
            this.contains = contains;
            this.notContains = notContains;
        }

        @Override
        public boolean accept(String className) {
            if (contains != null && !className.contains(contains)) {
                return false; // It does not contain a required string
            }
            if (notContains != null && className.contains(notContains)) {
                return false; // It contains a banned string
            }
            try {
                return Class.forName(className, false, contextClassLoader).isInterface();
            } catch (ClassNotFoundException e) {
                log.error("class {} not found!", className);
                return false;
            }
        }
    }
}
