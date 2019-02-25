package com.zys.jmeter.protocol.ssh.config;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.codec.Charsets;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zhuyongsheng on 2019/1/9.
 */
public class SshProperty {

    private static final JSch JSCH = new JSch();
    private static final int TIMEOUT = 6000;

    private Session session;

    public SshProperty(String user, String hostName, int port, String password) {

        try {
            session = JSCH.getSession(user, hostName, port); // 根据用户名，主机ip，端口获取一个Session对象
            session.setPassword(password); // 设置密码
            session.setConfig("StrictHostKeyChecking", "no"); // 为Session对象设置properties
            session.setTimeout(TIMEOUT); // 设置timeout时间
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }


    public String sendCmd(String cmd) {

        StringBuilder sb = new StringBuilder();
        try {
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(cmd);
            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);
            channelExec.connect();
            InputStream in = channelExec.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
            String buf;
            while ((buf = reader.readLine()) != null) {
                sb.append(buf).append("\n");
            }
            reader.close();
            channelExec.disconnect();
        } catch (Exception e) {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

    public void close() {
        this.session.disconnect();
    }

}
