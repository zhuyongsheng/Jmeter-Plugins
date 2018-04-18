package org.zys.jmeter.protocol.rpc.sampler;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 01369755 on 2018/3/24.
 */

public class RpcSampler extends AbstractSampler {

    private static final Logger log = LoggerFactory.getLogger(RpcSampler.class);

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    private static ApplicationConfig DUBBOSAMPLER = new ApplicationConfig("dubboSampler");

    private static ConcurrentHashMap<String, ReferenceConfig> clients = new ConcurrentHashMap();

    public static String PROTOCOL = "protocol";
    public static String HOST = "host";
    public static String PORT = "port";
    public static String INTERFACE_CLASS = "interfaceCls";
    public static String METHOD = "method";
    public static String VERSION = "version";
    public static String ARGS = "args";

    private String protocol;
    private String host;
    private String port;
    private String interfaceCls;
    private String methodInfo;
    private String version;
    private String args;

    private void init() throws Exception {
        protocol = getPropertyAsString(PROTOCOL).trim();
        host = getPropertyAsString(HOST).trim();
        port = getPropertyAsString(PORT).trim();
        interfaceCls = getPropertyAsString(INTERFACE_CLASS);
        version = getPropertyAsString(VERSION).trim();
        args = getPropertyAsString(ARGS).trim().replace("\n", "").replace("\t","");
        methodInfo = getPropertyAsString(METHOD);

    }

    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        try {
            init();
            res.sampleStart();
            res.setSamplerData(interfaceCls + "." + methodInfo + "?\n"  + args);
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
            return res;
        } finally {
            res.sampleEnd();
        }
        return res;
    }

    public String visitDubboService() throws Exception {

        ReferenceConfig ref = getReference(protocol, host, port, interfaceCls, version);
        Class paramType = Class.forName(methodInfo.substring(methodInfo.indexOf("(") + 1, methodInfo.indexOf(")")));
        Method method = Class.forName(interfaceCls).getDeclaredMethod(methodInfo.substring(0,methodInfo.indexOf("(")), paramType);
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(method.invoke(ref.get(), OBJECT_MAPPER.readValue(args, paramType)));
    }

    private ReferenceConfig getReference(String protocol, String host, String port, String interfaceCls, String version) throws Exception{
        StringBuffer key = new StringBuffer(host).append("_").append(interfaceCls).append("_").append(version);
        if (!clients.containsKey(key.toString())){
            ReferenceConfig ref = new ReferenceConfig();
            ref.setApplication(DUBBOSAMPLER);
            ref.setInterface(interfaceCls);
            ref.setVersion(version);
            switch (protocol) {
                case "dubbo" :
                    ref.setUrl(new StringBuffer(protocol).append("://").append(host).append(":").append(port).append("/").append(interfaceCls).toString());
                    break;
                case "zookeeper" :
                    RegistryConfig registryConfig= new RegistryConfig(new StringBuffer(protocol).append("://").append(host).append(":").append(port).toString());
                    ref.setRegistry(registryConfig);
                    break;
                default :
                    throw new Exception("unsupported protocol.");
            }
            clients.put(key.toString(), ref);
        }
        return clients.get(key.toString());
    }
}
