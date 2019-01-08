package com.zys.jmeter.protocol.hbase.config;

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
        try {
            setProperty(new ObjectProperty(hbaseName, new HbaseProperty(zkAddr)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public void testEnded() {
        ((HbaseProperty) getProperty(hbaseName).getObjectValue()).close();
        removeProperty(hbaseName);
    }

    @Override
    public void testEnded(String host) {
        testEnded();
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
