package org.zys.jmeter.protocol.rpc.sampler;

import org.I0Itec.zkclient.IZkStateListener;

import com.alibaba.dubbo.config.RegistryConfig;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import org.zys.jmeter.protocol.rpc.sampler.util.ObjectMapperUtil;

/**
 * Created by 01369755 on 2018/3/14.
 */
public class RpcSampler extends AbstractSampler {

    public static String NAME = "RPC请求";
    public static String ZOOKEEPER = "ZK地址";
    public static String INTERFACE = "接口";
    public static String METHOD_NAME = "方法";
    public static String METHOD_INDEX = "方法索引";
    public static String VERSION = "版本";
    public static String ARGS = "请求参数";

    public static ApplicationConfig RPCSAMPLER = new ApplicationConfig("rpcSampler");

    public static final Logger log = LoggerFactory.getLogger(RpcSampler.class);

    private String zookeeper;
    private String interfaceName;
    private int    methodIndex;
    private String version;
    private String args;
    private String methodName;

    private String getZookeeper()
    {
        return getPropertyAsString(ZOOKEEPER);
    }

    private String getInterfaceName() {
        return getPropertyAsString(INTERFACE);
    }

    private int getMethodIndex() {
        return getPropertyAsInt(METHOD_INDEX);
    }

    private String getVersion() {
        return getPropertyAsString(VERSION);
    }

    private String getArgs() {
        return getPropertyAsString(ARGS);
    }

    private String getMethodName(){
        return getPropertyAsString(METHOD_NAME);
    }

    public SampleResult sample(Entry entry)
    {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        zookeeper = getZookeeper().trim();
        interfaceName = getInterfaceName().trim();
        methodIndex = getMethodIndex();
        methodName = getMethodName();
        version = getVersion().trim();
        args = getArgs().trim().replace("\n", "").replace("\t", "");
        res.setSamplerData(interfaceName + "." + methodName + "?\n" + args);
        try
        {
            res.sampleStart();
            res.setResponseData(visitRpcService(), "UTF-8");
            res.setSuccessful(true);
        } catch (Exception e) {
            res.setResponseCode("500");
            res.setResponseData(e.toString(), "UTF-8");
            res.setSuccessful(false);
            res.setResponseMessage("找不到服务或请求参数不匹配");
            return res;
        } finally {
            res.sampleEnd();
        }
        return res;
    }
    public String visitRpcService() throws InvocationTargetException, IllegalAccessException, IOException, ClassNotFoundException {
        ReferenceConfig ref = ReferenceConfigSinglet.getReference(zookeeper, interfaceName, version);
        Class interfaceClass = Class.forName(interfaceName);
        Method method = interfaceClass.getMethods()[methodIndex];
        Class<?>[] paramTypes = method.getParameterTypes();
        return ObjectMapperUtil.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(method.invoke(ref.get(), ObjectMapperUtil.objectMapper.readValue(args,paramTypes[0])));
    }

    private static class ReferenceConfigSinglet {

        private static ConcurrentHashMap<String, ReferenceConfig> clients = new ConcurrentHashMap<String, ReferenceConfig>();

        private ReferenceConfigSinglet() {
        }

        private static ReferenceConfig getReference(String zk, String cls, String version) throws IOException {
            String key = zk + cls + version;
            if (!clients.containsKey(key)) {
                ReferenceConfig ref = new ReferenceConfig();
                RegistryConfig registryConfig = new RegistryConfig(zk);
                ref.setRegistry(registryConfig);
                ref.setApplication(RPCSAMPLER);
                ref.setInterface(cls);
                ref.setVersion(version);
                clients.put(key, ref);
            }
            return clients.get(key);
        }
    }
}
