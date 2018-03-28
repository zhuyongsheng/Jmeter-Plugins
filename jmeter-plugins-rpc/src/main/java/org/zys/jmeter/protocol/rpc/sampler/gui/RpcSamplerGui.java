package org.zys.jmeter.protocol.rpc.sampler.gui;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.rpc.sampler.RpcSampler;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

/**
 * Created by 01369755 on 2018/3/26.
 */
public class RpcSamplerGui extends AbstractSamplerGui {

    private static final Logger log = LoggerFactory.getLogger(RpcSamplerGui.class);

    private JLabeledTextField protocol;
    private JLabeledTextField host;
    private JLabeledTextField port;
    private JLabeledTextField interfaceCls;
    private JLabeledTextField version;
    private JComboBox<String> method;
    private JSyntaxTextArea   args;

    public RpcSamplerGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    public String getStaticLabel() {
        return "RPC请求";
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
        testElement.setProperty(RpcSampler.INTERFACE_CLASS, interfaceCls.getText());
        if (method.getSelectedItem() != null) {
            testElement.setProperty(RpcSampler.METHOD ,method.getSelectedIndex());
        }else {
            testElement.setProperty(RpcSampler.METHOD , Integer.valueOf(0));
        }
        testElement.setProperty(RpcSampler.VERSION, version.getText());
        testElement.setProperty(RpcSampler.ARGS, args.getText());
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
        interfaceCls.setText(element.getPropertyAsString(RpcSampler.INTERFACE_CLASS));
        method.removeAllItems();
        if (StringUtils.isNotEmpty(interfaceCls.getText())) {
            try {
                for (Method m : Class.forName(interfaceCls.getText()).getDeclaredMethods())
                    method.addItem(m.getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            method.setSelectedIndex(element.getPropertyAsInt(RpcSampler.METHOD));
        }
        args.setInitialText(element.getPropertyAsString(RpcSampler.ARGS));
//        args.setCaretPosition(0);
    }

    public void clearGui() {
        super.clearGui();
        protocol.setText("");
        host.setText("");
        port.setText("");
        interfaceCls.setText("");
        version.setText("");
        method.removeAllItems();
        args.setInitialText("");
        args.setCaretPosition(0);
    }

    private JPanel creatRequestPanel() {
        JPanel requestPanel = new VerticalPanel();
        requestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Dubbo请求"));
        requestPanel.add(createInterfacePanel(), BorderLayout.NORTH);
        requestPanel.add(createArgsPanel(), BorderLayout.CENTER);
        return requestPanel;
    }

    private JPanel createArgsPanel() {
        args = JSyntaxTextArea.getInstance(20, 20);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(makeBorder());
        panel.add(JTextScrollPane.getInstance(args), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInterfacePanel() {
        interfaceCls = new JLabeledTextField(" 接口：", 40);
        method = new JComboBox<>();
        JLabel label = new JLabel(" 方法:  ");
        label.setLabelFor(method);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.WEST);
        panel.add(method, BorderLayout.CENTER);
        version = new JLabeledTextField("  版本：", 7);

        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.add(interfaceCls, BorderLayout.WEST);
        webServerPanel.add(panel, BorderLayout.CENTER);
        webServerPanel.add(version, BorderLayout.EAST);
        return webServerPanel;
    }

    private JPanel createDubboServerPanel() {
        protocol = new JLabeledTextField(JMeterUtils.getResString("protocol"), 4); // $NON-NLS-1$
        host = new JLabeledTextField(JMeterUtils.getResString("web_server_domain"), 40); // $NON-NLS-1$
        port = new JLabeledTextField(JMeterUtils.getResString("web_server_port"), 7); // $NON-NLS-1$

        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "服务信息"));
        webServerPanel.add(protocol);
        webServerPanel.add(host);
        webServerPanel.add(port);
        return webServerPanel;
    }
}