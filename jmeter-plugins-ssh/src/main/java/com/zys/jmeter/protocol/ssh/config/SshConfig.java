package com.zys.jmeter.protocol.ssh.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuyongsheng on 2018/3/23.
 */
public class SshConfig extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(SshConfig.class);

    private String hostName;
    private int port;
    private String user;
    private String password;

    @Override
    public void testStarted() {
        setProperty(new ObjectProperty(hostName, new SshProperty(user, hostName, port, password)));
    }

    @Override
    public void testStarted(String s) {
        testStarted();
    }

    @Override
    public void testEnded() {
        ((SshProperty) getProperty(hostName).getObjectValue()).close();
    }

    @Override
    public void testEnded(String s) {
        testEnded();
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
