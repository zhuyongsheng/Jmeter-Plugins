package org.zys.jmeter.protocol.rpc.sampler.util;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
 * Created by 01369755 on 2018/6/1.
 */
public class RpcUtils {

    private static final Logger log = LoggerFactory.getLogger(RpcUtils.class);

    private static Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();
    private static ApplicationConfig DUBBOSAMPLER = new ApplicationConfig("dubboSampler");
    private static int TIMEOUT = 5000;

    private static final Map<String, Map<String, List<String>>> treeMap = new HashMap<>();
    private static final Map<String, Method> methodMap = new HashMap<>();

    private static final String[] SPATHS = new String[]{
            JMeterUtils.getJMeterHome() + "/lib/dubbo"             //需将/lib/dubbo加入user.classpath配置中，否则无法加载类
    };

    public static String invokeMethod(ReferenceConfig ref, Method method, Object[] args) throws Exception {
        return GSON.toJson(method.invoke(ref.get(), args));
    }

    public static Method getMethod(String className, String methodName){
        return  methodMap.get(className + "." + methodName);
    }

    public static String[] getMethodNames(String interfaceCls) {
        if (null == treeMap.get(interfaceCls)) {
            if (StringUtils.isNotEmpty(interfaceCls)) {
                Map<String, List<String>> mMap = new HashMap<>();
                try {
                    for (Method m : Class.forName(interfaceCls).getDeclaredMethods()) {

                        Class[] pts = m.getParameterTypes();
                        List<String> paramTypes = new ArrayList<>();
                        if (null != pts && pts.length > 0) {
                            for (Class pt : pts) {
                                paramTypes.add(pt.getName());
                            }
                        }
                        mMap.put(m.getName(), paramTypes);
                        methodMap.put(interfaceCls + "." + m.getName(), m);

                    }
                } catch (ClassNotFoundException e) {
                    log.error("class {} not found!", interfaceCls);
                }
                treeMap.put(interfaceCls, mMap);
            }
        }
        return treeMap.get(interfaceCls).keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public static String[] getClassNames(){
        if (treeMap.size() == 0) {
            try {
                Iterator<String> it = ClassFinder.findClasses(SPATHS, new InterfaceFilter("Service", "RestService")).iterator();
                while (it.hasNext()) {
                    String clazz = it.next();
                    treeMap.put(clazz, null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return treeMap.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public static List<String> getparamTypes(String clz, String method){
        return treeMap.get(clz).get(method);
    }

    public static Object[] getArgs(Method method, String[] args){
        Class[] types = method.getParameterTypes();
        List<Object> argList = new ArrayList<>();
        for (int i=0; i<types.length; i++){
            argList.add(GSON.fromJson(args[i], types[i]));
        }
        return argList.toArray(ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /*Jmeter插件设计时，对于中间件（如redis，Hbase..）一般采用JMeterProperty，
    因为中间件对于一套SUT来说，信息是确定的，是全局的，并且一般只用来做信息验证，不需要区分线程；
    对于SUT对外接口（如RPC）则需要采用JMeterVariables，以在多线程时，能够更好的模拟多个用户。*/
    public static ReferenceConfig getReference(String protocol, String host, String port, String clsName, String version, String group) throws Exception {
        StringBuffer key = new StringBuffer(host).append("_").append(clsName).append("_").append(version).append("_").append(group);
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        Object object = variables.getObject(key.toString());
        if (null == object) {
            ReferenceConfig ref = new ReferenceConfig();
            ref.setApplication(DUBBOSAMPLER);
            ref.setInterface(clsName);
            ref.setVersion(version);
            ref.setGroup(group);
            ref.setTimeout(TIMEOUT);
            switch (protocol) {
                case "dubbo":
                    ref.setUrl(new StringBuffer(protocol).append("://").append(host).append(":").append(port).append("/").append(clsName).toString());
                    break;
                case "zookeeper":
                    ref.setRegistry(new RegistryConfig(new StringBuffer(protocol).append("://").append(host).append(":").append(port).toString()));
                    break;
                default:
                    throw new Exception("unsupported protocol.");
            }
            variables.putObject(key.toString(), ref);
            return ref;
        }
        return (ReferenceConfig)object;
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
            }
            return false;
        }
    }
}
