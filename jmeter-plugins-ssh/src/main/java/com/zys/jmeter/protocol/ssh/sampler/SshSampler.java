package com.zys.jmeter.protocol.ssh.sampler;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.zys.jmeter.protocol.ssh.config.SshConfig;
import jodd.log.Logger;
import jodd.log.LoggerFactory;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by 01369755 on 2018/1/3.
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
            res.setResponseData(sendCmd(),"UTF-8");
            res.setResponseCode("0");
            res.setSuccessful(true);

        } catch (Exception e) {
            res.setResponseMessage(e.toString());
            res.setResponseCode("500");
            res.setSuccessful(false);
        } finally {
            res.sampleEnd();
            return res;
        }
    }

    public String sendCmd(){

        StringBuffer sb = new StringBuffer();
        try{
            Session session = SshConfig.sessionMap.get(host);
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(cmd);
            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);
            channelExec.connect();
            InputStream in = channelExec.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
            String buf = null;
            while ((buf = reader.readLine()) != null) {
                sb.append(buf).append("\n");
            }
            reader.close();
            channelExec.disconnect();
        }catch (Exception e){
            sb.append(e.getMessage());
        }
        return sb.toString();
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
