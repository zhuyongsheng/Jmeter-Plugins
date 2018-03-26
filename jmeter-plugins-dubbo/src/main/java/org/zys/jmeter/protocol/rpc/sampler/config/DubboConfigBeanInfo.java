package org.zys.jmeter.protocol.rpc.sampler.config;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

/**
 * Created by 01369755 on 2018/3/24.
 */
public class DubboConfigBeanInfo extends BeanInfoSupport {

    public DubboConfigBeanInfo ()

    {
        super(DubboConfig.class);
        createPropertyGroup("Service信息", new String[] { "serviceName", "zookeeper", "interfaceName", "version"});
        PropertyDescriptor p = property("serviceName");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("zookeeper");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("interfaceName");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("version");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
    }
}
