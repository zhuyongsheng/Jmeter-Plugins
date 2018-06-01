package org.zys.jmeter.protocol.rpc.sampler;

import com.alibaba.dubbo.config.ReferenceConfig;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.rpc.sampler.util.RpcUtils;

import java.lang.reflect.Method;

/**
 * Created by zhuyongsheng on 2018/3/24.
 */

public class RpcSampler extends AbstractSampler {

    private static final Logger log = LoggerFactory.getLogger(RpcSampler.class);

    public static String PROTOCOL = "protocol";
    public static String HOST = "host";
    public static String PORT = "port";
    public static String INTERFACE_CLASS = "interfaceCls";
    public static String METHOD = "method";
    public static String VERSION = "version";
    public static String GROUP = "group";
    public static String ARGUMENTS = "arguments";

    private String protocol;
    private String host;
    private String port;
    private String clsName;
    private String methodName;
    private String version;
    private String group;
    private Arguments arguments;

    private void init() throws Exception {
        protocol = getPropertyAsString(PROTOCOL).trim();
        host = getPropertyAsString(HOST).trim();
        port = getPropertyAsString(PORT).trim();
        clsName = getPropertyAsString(INTERFACE_CLASS);
        version = getPropertyAsString(VERSION).trim();
        group = getPropertyAsString(GROUP).trim();
        methodName = getPropertyAsString(METHOD);
        arguments = ((Arguments) getProperty(ARGUMENTS).getObjectValue());
    }

    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        try {
            init();
            res.setSamplerData(clsName + "." + methodName + "?\n" + arguments.toString());
            res.sampleStart();
            ReferenceConfig ref = RpcUtils.getReference(protocol, host, port, clsName, version, group);
            Method method = RpcUtils.getMethod(clsName, methodName);
            Object[] args = RpcUtils.getArgs(method, arguments.getArgumentsAsMap().values().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            res.setResponseData(RpcUtils.invokeMethod(ref, method, args),"UTF-8");
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

}
