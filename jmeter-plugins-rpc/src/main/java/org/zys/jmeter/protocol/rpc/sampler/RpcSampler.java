package org.zys.jmeter.protocol.rpc.sampler;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by zhuyongsheng on 2018/3/24.
 */

public class RpcSampler extends AbstractSampler {

    private static final Logger log = LoggerFactory.getLogger(RpcSampler.class);


    private static Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();
    private static ApplicationConfig DUBBOSAMPLER = new ApplicationConfig("dubboSampler");
    private static int TIMEOUT = 5000;

    public static String PROTOCOL = "protocol";
    public static String HOST = "host";
    public static String PORT = "port";
    public static String INTERFACE_CLASS = "interfaceCls";
    public static String METHOD = "method";
    public static String VERSION = "version";
    public static String GROUP = "group";
    public static String ARGS = "args";

    private String protocol;
    private String host;
    private String port;
    private String interfaceCls;
    private String methodInfo;
    private String version;
    private String group;
    private String args;

    private void init() throws Exception {
        protocol = getPropertyAsString(PROTOCOL).trim();
        host = getPropertyAsString(HOST).trim();
        port = getPropertyAsString(PORT).trim();
        interfaceCls = getPropertyAsString(INTERFACE_CLASS);
        version = getPropertyAsString(VERSION).trim();
        group = getPropertyAsString(GROUP).trim();
        args = getPropertyAsString(ARGS).trim().replace("\n", "").replace("\t", "");
        methodInfo = getPropertyAsString(METHOD);
    }

    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        try {
            init();
            res.setSamplerData(interfaceCls + "." + methodInfo + "?\n" + args);
            res.sampleStart();
            res.setResponseData(visitDubboService(), "UTF-8");
            res.setResponseCode("0");
            res.setSuccessful(true);
            res.setResponseMessage("OK");
        } catch (Exception e) {
            res.setResponseData(e.toString(), "UTF-8");
            res.setResponseCode("500");
            res.setSuccessful(false);
            res.setResponseMessage(e.getMessage());
            e.printStackTrace();
        } finally {
            res.sampleEnd();
        }
        return res;
    }

    public String visitDubboService() throws Exception {

        ReferenceConfig ref = getReference(protocol, host, port, interfaceCls, version, group);

        Class paramType = Class.forName(methodInfo.substring(methodInfo.indexOf("(") + 1, methodInfo.indexOf(")")));
        Method method = Class.forName(interfaceCls).getDeclaredMethod(methodInfo.substring(0, methodInfo.indexOf("(")), paramType);
        return GSON.toJson(method.invoke(ref.get(), GSON.fromJson(args, paramType)));
    }

    //    为以后多参数扩展做准备
    private Class[] getparamTypes(String paramTypesList) throws ClassNotFoundException {
        String[] paramTypes = paramTypesList.split(",");
        Class[] paramType = new Class[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramType[i] = Class.forName(paramTypes[i]);
        }
        return paramType;
    }

    /*Jmeter插件设计时，对于中间件（如redis，Hbase..）一般采用JMeterProperty，
    因为中间件对于一套SUT来说，信息是确定的，是全局的，并且一般只用来做信息验证，不需要区分线程；
    对于SUT对外接口（如RPC）则需要采用JMeterVariables，以在多线程时，能够更好的模拟多个用户。*/
    private ReferenceConfig getReference(String protocol, String host, String port, String interfaceCls, String version, String group) throws Exception {
        StringBuffer key = new StringBuffer(host).append("_").append(interfaceCls).append("_").append(version).append("_").append(group);
        JMeterVariables variables = getThreadContext().getVariables();
        Object object = variables.getObject(key.toString());
        if (null == object) {
            ReferenceConfig ref = new ReferenceConfig();
            ref.setApplication(DUBBOSAMPLER);
            ref.setInterface(interfaceCls);
            ref.setVersion(version);
            ref.setGroup(group);
            ref.setTimeout(TIMEOUT);
            switch (protocol) {
                case "dubbo":
                    ref.setUrl(new StringBuffer(protocol).append("://").append(host).append(":").append(port).append("/").append(interfaceCls).toString());
                    break;
                case "zookeeper":
                    RegistryConfig registryConfig = new RegistryConfig(new StringBuffer(protocol).append("://").append(host).append(":").append(port).toString());
                    ref.setRegistry(registryConfig);
                    break;
                default:
                    throw new Exception("unsupported protocol.");
            }
            variables.putObject(key.toString(), ref);
            return ref;
        }
        return (ReferenceConfig) object;
    }
}
