package com.zys.jmeter.protocol.ssh.config;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;
import java.beans.PropertyDescriptor;

/**
 * Created by zhuyongsheng on 2018/3/23.
 */
public class SshConfigBeanInfo extends BeanInfoSupport {

    public SshConfigBeanInfo ()

    {
        super(SshConfig.class);
        createPropertyGroup("SSH主机信息", new String[] { "hostName", "port", "user", "password"});
        PropertyDescriptor p = property("hostName");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("port");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, 22);
        p = property("user");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("password", TypeEditor.PasswordEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
    }
}
