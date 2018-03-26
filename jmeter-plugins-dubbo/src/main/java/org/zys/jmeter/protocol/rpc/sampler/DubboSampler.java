package org.zys.jmeter.protocol.rpc.sampler;

import com.alibaba.dubbo.config.ReferenceConfig;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.rpc.sampler.config.DubboConfig;
import org.zys.jmeter.protocol.rpc.sampler.util.ObjectMapperUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by 01369755 on 2018/3/24.
 */
public class DubboSampler extends AbstractSampler {

    private static final Logger log = LoggerFactory.getLogger(DubboSampler.class);

    private String serviceName;
    private Method method;
    private String args;

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(serviceName + "." + method.getName() + "?\n" + args);
        try
        {
            res.sampleStart();
            res.setResponseData(visitDubboService(), "UTF-8");
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

    public String visitDubboService() throws InvocationTargetException, IllegalAccessException, IOException, ClassNotFoundException {
        ReferenceConfig ref = DubboConfig.consumerMap.get(serviceName);
        Class<?>[] paramTypes = method.getParameterTypes();
        return ObjectMapperUtil.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(method.invoke(ref.get(), ObjectMapperUtil.objectMapper.readValue(args,paramTypes[0])));
    }


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args.trim().replace("\n", "").replace("\t", "");
    }
}
