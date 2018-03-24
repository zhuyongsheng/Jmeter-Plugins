package org.zys.jmeter.protocol.rpc.sampler.config;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 01369755 on 2018/3/24.
 */
public class DubboConfig  extends ConfigTestElement implements TestBean, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(DubboConfig.class);

    public static ApplicationConfig RPCSAMPLER = new ApplicationConfig("rpcSampler");

    private String serviceName;
    private String zookeeper;
    private String interfaceName;
    private String version;

    public static ConcurrentHashMap<String, ReferenceConfig> consumerMap = new ConcurrentHashMap<>();

    @Override
    public void testStarted() {
        initReference();
    }

    @Override
    public void testStarted(String s) {
        testStarted();
    }

    @Override
    public void testEnded() {
        closeReference();
    }

    @Override
    public void testEnded(String s) {
        testEnded();
    }

    private void initReference(){
        ReferenceConfig ref = new ReferenceConfig();
        RegistryConfig registryConfig = new RegistryConfig(zookeeper);
        ref.setRegistry(registryConfig);
        ref.setApplication(RPCSAMPLER);
        ref.setInterface(interfaceName);
        ref.setVersion(version);
        consumerMap.put(serviceName, ref);
    }

    private void closeReference(){
        consumerMap.get(serviceName).destroy();
        consumerMap.remove(serviceName);
    }


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(String zookeeper) {
        this.zookeeper = zookeeper;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
