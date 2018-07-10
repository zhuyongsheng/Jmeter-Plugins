package org.zys.jmeter.protocol.rpc.sampler.gui;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.rpc.sampler.RpcSampler;
import org.zys.jmeter.protocol.rpc.sampler.util.RpcUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Created by zhuyongsheng on 2018/3/26.
 */
public class RpcSamplerGui extends AbstractSamplerGui {

    private static final Logger log = LoggerFactory.getLogger(RpcSamplerGui.class);

    private JLabeledTextField protocol;
    private JLabeledTextField host;
    private JLabeledTextField port;
    private JLabeledTextField version;
    private JLabeledTextField group;
    private JLabeledChoice className;
    private JLabeledChoice methodName;
    private ArgumentsPanel argsPanel;

    public RpcSamplerGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    public String getStaticLabel() {
        return "DUBBO请求";
    }


    @Override
    public TestElement createTestElement() {
        RpcSampler sampler = new RpcSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement testElement) {
        testElement.clear();
        configureTestElement(testElement);
        testElement.setProperty(RpcSampler.PROTOCOL, protocol.getText());
        testElement.setProperty(RpcSampler.HOST, host.getText());
        testElement.setProperty(RpcSampler.PORT, port.getText());
        testElement.setProperty(RpcSampler.INTERFACE_CLASS, className.getText());
        testElement.setProperty(RpcSampler.METHOD, methodName.getText());
        testElement.setProperty(RpcSampler.VERSION, version.getText());
        testElement.setProperty(RpcSampler.GROUP, group.getText());
        testElement.setProperty(new TestElementProperty(RpcSampler.ARGUMENTS, argsPanel.createTestElement()));
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createDubboServerPanel());
        add(box, BorderLayout.NORTH);
        JPanel panel = creatRequestPanel();
        add(panel, BorderLayout.CENTER);
        add(Box.createVerticalStrut(panel.getPreferredSize().height), BorderLayout.WEST);
    }

    public void configure(TestElement element) {

        super.configure(element);
        protocol.setText(element.getPropertyAsString(RpcSampler.PROTOCOL));
        host.setText(element.getPropertyAsString(RpcSampler.HOST));
        port.setText(element.getPropertyAsString(RpcSampler.PORT));
        version.setText(element.getPropertyAsString(RpcSampler.VERSION));
        group.setText(element.getPropertyAsString(RpcSampler.GROUP));
        className.setText(element.getPropertyAsString(RpcSampler.INTERFACE_CLASS));
        methodName.setText(element.getPropertyAsString(RpcSampler.METHOD));
        argsPanel.configure((Arguments) element.getProperty(RpcSampler.ARGUMENTS).getObjectValue());
    }

    public void clearGui() {
        super.clearGui();
        protocol.setText("");
        host.setText("");
        port.setText("");
        className.setSelectedIndex(-1);
        methodName.setValues(ArrayUtils.EMPTY_STRING_ARRAY);
        methodName.setSelectedIndex(-1);
        version.setText("");
        group.setText("");
        argsPanel.clearGui();
    }

    private JPanel creatRequestPanel() {
        JPanel requestPanel = new VerticalPanel();
        requestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dubbo请求"));
        requestPanel.add(createInterfacePanel(), BorderLayout.NORTH);
        requestPanel.add(createArgsPanel(), BorderLayout.CENTER);
        return requestPanel;
    }

    private JPanel createArgsPanel() {
        argsPanel = new ArgumentsPanel(JMeterUtils.getResString("paramtable")); // $NON-NLS-1$
        return argsPanel;
    }

    private JPanel createInterfacePanel() {
        className = new JLabeledChoice(" 接口:  ", RpcUtils.getClassNames());
        className.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                if (evt.getSource() == className) {
                    methodName.setValues(RpcUtils.getMethodNames(className.getText()));
                }
            }
        });
        methodName = new JLabeledChoice(" 方法:  ", ArrayUtils.EMPTY_STRING_ARRAY);
        methodName.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == methodName) {
                    setupArgs();
                }
            }
        });
        version = new JLabeledTextField(" 版本：", 6);
        group = new JLabeledTextField(" 群组：", 6);

        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.add(className);
        webServerPanel.add(methodName);
        webServerPanel.add(version);
        webServerPanel.add(group);
        return webServerPanel;
    }

    private JPanel createDubboServerPanel() {
        protocol = new JLabeledTextField(JMeterUtils.getResString("protocol"), 4); // $NON-NLS-1$
        host = new JLabeledTextField(JMeterUtils.getResString("web_server_domain"), 40); // $NON-NLS-1$
        port = new JLabeledTextField(JMeterUtils.getResString("web_server_port"), 7); // $NON-NLS-1$

        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "服务信息"));
        webServerPanel.add(protocol);
        webServerPanel.add(host);
        webServerPanel.add(port);
        return webServerPanel;
    }

    private void setupArgs() {
        Arguments arguments = new Arguments();
        for (String paramType : RpcUtils.getparamTypes(methodName.getText())) {
            if (!paramType.isEmpty()) {
                arguments.addArgument(paramType, "");
            }
        }
        argsPanel.configure(arguments);
    }


}