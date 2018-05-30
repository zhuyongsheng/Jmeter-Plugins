package com.zys.jmeter.protocol.hbase.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhuyongsheng on 2018/4/25.
 */
public class HbaseConfig extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(HbaseConfig.class);

//    private static Map<String, Connection> HBASE_CLIENTS= new HashMap<>();

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
            ((Connection)getProperty(hbaseName)).close();
            removeProperty(hbaseName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }

    /*public static Connection getConnection(String hbaseName){
        return HBASE_CLIENTS.get(hbaseName);
    }*/

    private void initConnection() {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", zkAddr);
        try {
            Connection connection = ConnectionFactory.createConnection(conf);
            setProperty(new ObjectProperty(hbaseName, connection));
//            HBASE_CLIENTS.put(hbaseName, connection);
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
