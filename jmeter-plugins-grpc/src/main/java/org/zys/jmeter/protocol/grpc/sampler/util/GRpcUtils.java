package org.zys.jmeter.protocol.grpc.sampler.util;

/*
@Time : 2020/5/29 5:00 下午
@Author : yongshengzhu
*/

import com.google.protobuf.MessageOrBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFilter;
import org.apache.jorphan.reflect.ClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.Message.Builder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GRpcUtils {

    private static final Logger log = LoggerFactory.getLogger(GRpcUtils.class);

    private static final String GRPC_SUFFIX = "Grpc";
    private static final String BLOCKING_STUB_SUFFIX = "BlockingStub";
    private static final String NEW_BLOCKING_STUB = "newBlockingStub";
    private static final String NEW_BUILDER = "newBuilder";
    private static final String BUILD_METHOD = "build";

    private static final Map<String, Map<String, Method>> serviceMap = new HashMap<>();
    private static final String[] SPATHS = new String[]{
            JMeterUtils.getJMeterHome() + "/lib/grpc" //需将/lib/grpc加入user.classpath配置中，以加载类
    };
    private static final String[] EMPTY_METHOD = new String[]{StringUtils.EMPTY};
    private static String[] CLASS_NAMES;

    private GRpcUtils() {
    }

    public static String invoke(String host, int port, boolean secure, String clsName, String methodName, Collection<String> args) throws Exception {
        Object remoteObject = getClient(host, port, secure, clsName);
        Method method = getMethod(clsName, methodName);
        Object[] objects = getArgs(method, args.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        return JsonFormat.printer().print((MessageOrBuilder) method.invoke(remoteObject, objects));
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
                for (Class c : Class.forName(clsName).getDeclaredClasses()) {
                    if (c.getName().endsWith(BLOCKING_STUB_SUFFIX)) {
                        for (Method m : c.getDeclaredMethods()) {
                            if (m.getName() == BUILD_METHOD) {
                                continue;
                            }
                            mMap.put(getMethodName(m), m);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                log.error("class {} not found!", clsName);
            }
            serviceMap.put(clsName, mMap);
        }
        String[] methods = serviceMap.get(clsName).keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        Arrays.sort(methods);
        return methods;
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
                ClassFinder.findClasses(SPATHS, new GRpcServiceFilter())
                        .forEach(clazz -> serviceMap.put(clazz, null));
            } catch (IOException e) {
                log.warn(e.toString());
            }
            CLASS_NAMES = serviceMap.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
            Arrays.sort(CLASS_NAMES);
        }
        return CLASS_NAMES;
    }

    private static Object[] getArgs(Method method, String[] args) throws Exception {
        Class<?>[] types = method.getParameterTypes();
        Object[] objects = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Method builderMethod = types[i].getMethod(NEW_BUILDER);
            Builder builder = (Builder) builderMethod.invoke(null);
            JsonFormat.parser().merge(args[i], builder);
            objects[i] = builder.build();
        }
        return objects;
    }

    private static String getAddressKey(String host, int port) {
        return host + "_" + port;
    }

    private static String getStubKey(String host, int port, String clsName) {
        return getAddressKey(host, port) + "_" + clsName;
    }

    private static ManagedChannel getChannel(String host, int port, boolean secure) {
        String key = getAddressKey(host, port);
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        Object object = variables.getObject(key);
        if (null == object) {
            ManagedChannelBuilder builder = ManagedChannelBuilder.forAddress(host, port);
            if (!secure) {
                builder.usePlaintext();
            }
            ManagedChannel channel = builder.build();
            variables.putObject(key, channel);
            return channel;
        }
        return (ManagedChannel) object;
    }

    public static void shutdown(String host, int port, String clsName) {
        String addressKey = getAddressKey(host, port);
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        Object object = variables.getObject(addressKey);
        if (null == object) {
            return;
        }
        ((ManagedChannel) object).shutdown();
        variables.remove(addressKey);
        String stubKey = getStubKey(host, port, clsName);
        variables.remove(stubKey);
    }

    private static Object getClient(String host, int port, boolean secure, String clsName) throws Exception {
        String key = getStubKey(host, port, clsName);
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        Object object = variables.getObject(key);
        if (null == object) {
            ManagedChannel channel = getChannel(host, port, secure);
//            反射获取stub
            Class cls = Class.forName(clsName);
            Method newBlockingStub = cls.getDeclaredMethod(NEW_BLOCKING_STUB, io.grpc.Channel.class);
            Object stub = newBlockingStub.invoke(cls, channel);
            variables.putObject(key, stub);
            return stub;
        }
        return object;
    }

    private static class GRpcServiceFilter implements ClassFilter {

        GRpcServiceFilter() {
        }

        @Override
        public boolean accept(String className) {
            return className.endsWith(GRPC_SUFFIX);
        }
    }
}
