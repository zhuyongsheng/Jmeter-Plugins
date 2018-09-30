package org.zys.jmeter.protocol.rpc.sampler;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.rpc.sampler.util.RpcUtils;

/**
 * Created by zhuyongsheng on 2018/3/24.
 */

public class RpcSampler extends AbstractSampler {

    private static final Logger log = LoggerFactory.getLogger(RpcSampler.class);

    public static final String PROTOCOL = "协议：";
    public static final String HOST = "服务器名称或IP：";
    public static final String PORT = "端口：";
    public static final String CLASSNAME = "接口：";
    public static final String METHOD = "方法：";
    public static final String VERSION = "版本：";
    public static final String GROUP = "群组：";
    public static final String ARGUMENTS = "同请求一起发送参数：";

    public SampleResult sample(Entry entry) {
        String protocol = getPropertyAsString(PROTOCOL).trim();
        String host = getPropertyAsString(HOST).trim();
        int port = getPropertyAsInt(PORT);
        String clsName = getPropertyAsString(CLASSNAME);
        String version = getPropertyAsString(VERSION).trim();
        String group = getPropertyAsString(GROUP).trim();
        String methodName = getPropertyAsString(METHOD);
        Arguments arguments = (Arguments) getProperty(ARGUMENTS).getObjectValue();
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(clsName + "." + methodName + "?\n" + arguments.toString());
        res.sampleStart();
        try {
            res.setResponseData(RpcUtils.invoke(protocol, host, port, clsName, version, group, methodName, arguments.getArgumentsAsMap().values()), "UTF-8");
            res.setResponseCode("0");
            res.setSuccessful(true);
            res.setResponseMessage("OK");
        } catch (Exception e) {
            e.printStackTrace();
            res.setResponseData(e.toString(), "UTF-8");
            res.setResponseCode("500");
            res.setSuccessful(false);
            res.setResponseMessage(e.getMessage());
        } finally {
            res.sampleEnd();
        }
        return res;
    }


}
