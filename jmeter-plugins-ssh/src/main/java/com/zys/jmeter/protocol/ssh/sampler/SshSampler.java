package com.zys.jmeter.protocol.ssh.sampler;

import com.zys.jmeter.protocol.ssh.config.SshProperty;
import jodd.log.Logger;
import jodd.log.LoggerFactory;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;

/**
 * Created by zhuyongsheng on 2018/1/3.
 */
public class SshSampler extends AbstractSampler implements TestBean{

    private static final Logger log = LoggerFactory.getLogger(SshSampler.class);


    private String host;
    private String cmd;

    public SampleResult sample(Entry entry) {

        SampleResult res = new SampleResult();
        res.setSamplerData(cmd);
        res.setSampleLabel(getName());
        try {
            res.sampleStart();
            res.setResponseData(((SshProperty) getProperty(host).getObjectValue()).sendCmd(cmd),"UTF-8");
            res.setResponseCode("0");
            res.setSuccessful(true);

        } catch (Exception e) {
            res.setResponseMessage(e.toString());
            res.setResponseCode("500");
            res.setSuccessful(false);
        } finally {
            res.sampleEnd();
        }
        return res;
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
