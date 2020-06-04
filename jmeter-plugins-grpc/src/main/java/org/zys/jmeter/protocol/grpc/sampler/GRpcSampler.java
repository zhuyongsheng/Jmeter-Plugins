package org.zys.jmeter.protocol.grpc.sampler;

/*
@Time : 2020/5/29 5:00 下午
@Author : yongshengzhu
*/

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.grpc.sampler.util.GRpcUtils;

public class GRpcSampler extends AbstractSampler {

    private static final Logger log = LoggerFactory.getLogger(GRpcSampler.class);

    public static final String HOST = "服务器名称或IP：";
    public static final String PORT = "端口：";
    public static final String CLASS_NAME = "服务：";
    public static final String METHOD = "方法：";
    public static final String ARGUMENTS = "同请求一起发送参数：";
    public static final String SECURE = "加密";
    public static final String CLOSE_CHANNEL = "关闭连接";

    public SampleResult sample(Entry entry) {
        String host = getPropertyAsString(HOST).trim();
        int port = getPropertyAsInt(PORT);
        boolean secure = getPropertyAsBoolean(SECURE);
        String clsName = getPropertyAsString(CLASS_NAME);
        String methodName = getPropertyAsString(METHOD);
        Arguments arguments = (Arguments) getProperty(ARGUMENTS).getObjectValue();
        boolean closeChannel = getPropertyAsBoolean(CLOSE_CHANNEL);
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(clsName + "." + methodName + "?\n" + arguments.toString());
        res.sampleStart();
        try {
            res.setResponseData(GRpcUtils.invoke(host, port, secure, clsName, methodName, arguments.getArgumentsAsMap().values()), "UTF-8");
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
            if (closeChannel) {
                GRpcUtils.shutdown(host, port, clsName);
            }
        }
        return res;
    }


}

