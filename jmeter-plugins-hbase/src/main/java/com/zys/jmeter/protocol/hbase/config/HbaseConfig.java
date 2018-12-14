package com.zys.jmeter.protocol.hbase.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by zhuyongsheng on 2018/4/25.
 */
public class HbaseConfig extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(HbaseConfig.class);

    private String hbaseName;
    private String zkAddr;

    @Override
    public void testStarted() {
        initConnection();
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public void testEnded() {
        try {
            ((Connection) getProperty(hbaseName).getObjectValue()).close();
            removeProperty(hbaseName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }

    /**
     * 初始化Hbase连接，在windows环境下，如果没有配置hadoop环境变量，
     * org.apache.hadoop.util.shell类会打印找不到winutils.exe文件的ERROR日志，实际并不会影响Hbase的使用，
     * 可在jmeter/bin目录下log4j2.xml文件中配置关闭org.apache.hadoop.util包的日志：
     * <logger name="org.apache.hadoop.util" level="off"/>
     *
     * @author zhuyongsheng
     * @date 2018/8/21
     */
    private void initConnection() {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", zkAddr);
        try {
            setProperty(new ObjectProperty(hbaseName, ConnectionFactory.createConnection(conf)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHbaseName() {
        return hbaseName;
    }

    public void setHbaseName(String hbaseName) {
        this.hbaseName = hbaseName;
    }

    public String getZkAddr() {
        return zkAddr;
    }

    public void setZkAddr(String zkAddr) {
        this.zkAddr = zkAddr;
    }
}
