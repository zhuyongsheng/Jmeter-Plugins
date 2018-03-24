package com.zys.jmeter.protocol.ssh.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 01369755 on 2018/3/23.
 */
public class SshConfig extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(SshConfig.class);

    private static JSch JSCH = new JSch();
    private static int TIMEOUT = 6000;

    private String host;
    private int    port;
    private String user;
    private String password;
    private Session session;


    public static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    private void initSession(){
        try {
            session = JSCH.getSession(user, host, port); // 根据用户名，主机ip，端口获取一个Session对象
            session.setPassword(password); // 设置密码
            session.setConfig("StrictHostKeyChecking", "no"); // 为Session对象设置properties
            session.setTimeout(TIMEOUT); // 设置timeout时间
            session.connect();
            sessionMap.put(host, session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeSession(){
        try{
            sessionMap.get(host).disconnect();
            sessionMap.remove(host);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void testStarted() {
        initSession();
    }

    @Override
    public void testStarted(String s) {
        testStarted();
    }

    @Override
    public void testEnded() {
        closeSession();
    }

    @Override
    public void testEnded(String s) {
        testEnded();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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
