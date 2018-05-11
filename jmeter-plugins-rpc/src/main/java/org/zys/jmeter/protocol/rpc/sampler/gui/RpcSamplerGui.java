package org.zys.jmeter.protocol.rpc.sampler.gui;

import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.reflect.ClassFilter;
import org.apache.jorphan.reflect.ClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.rpc.sampler.RpcSampler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 01369755 on 2018/3/26.
 */
public class RpcSamplerGui extends AbstractSamplerGui implements ActionListener {

    private static final Logger log = LoggerFactory.getLogger(RpcSamplerGui.class);

    private static Map<String, List<String>> methodNames = new HashMap<>();

    private JLabeledTextField protocol;
    private JLabeledTextField host;
    private JLabeledTextField port;
    private JLabeledTextField version;
    private JSyntaxTextArea args;
    private JComboBox<String> classNameCombo;
    private JComboBox<String> methodNameCombo;


    private static final String[] SPATHS = new String[]{
            JMeterUtils.getJMeterHome() + "/lib/dubbo/" //$NON-NLS-1$
    };

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
        if (classNameCombo.getSelectedItem() != null) {
            testElement.setProperty(RpcSampler.INTERFACE_CLASS, classNameCombo.getSelectedItem().toString());
        }
        if (methodNameCombo.getSelectedItem() != null) {
            testElement.setProperty(RpcSampler.METHOD, methodNameCombo.getSelectedItem().toString());
        }
        testElement.setProperty(RpcSampler.VERSION, version.getText());
        testElement.setProperty(RpcSampler.ARGS, args.getText());
    }

    private void init() {
        log.info(System.getProperty("java.class.path"));
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
        classNameCombo.setSelectedItem(element.getPropertyAsString(RpcSampler.INTERFACE_CLASS));
        methodNameCombo.setSelectedItem(element.getPropertyAsString(RpcSampler.METHOD));
        args.setInitialText(element.getPropertyAsString(RpcSampler.ARGS));
    }

    public void clearGui() {
        super.clearGui();
        protocol.setText("");
        host.setText("");
        port.setText("");
        classNameCombo.setSelectedIndex(-1);
        methodNameCombo.setSelectedIndex(-1);
        version.setText("");
        args.setInitialText("");
        args.setCaretPosition(0);
    }

    private JPanel creatRequestPanel() {
        JPanel requestPanel = new VerticalPanel();
        requestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dubbo请求"));
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
        try {
            classNameCombo = new JComboBox(ClassFinder.findClasses(SPATHS,
                    new InterfaceFilter("Service", "RestService")).toArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        classNameCombo.addActionListener(this);
        JLabel classLabel = new JLabel(" 接口:  ");
        classLabel.setLabelFor(classNameCombo);
        JPanel classPanel = new JPanel(new BorderLayout());
        classPanel.add(classLabel, BorderLayout.WEST);
        classPanel.add(classNameCombo, BorderLayout.CENTER);

        methodNameCombo = new JComboBox<>();
        JLabel methodLabel = new JLabel(" 方法:  ");
        methodLabel.setLabelFor(methodNameCombo);
        JPanel methodPanel = new JPanel(new BorderLayout());
        methodPanel.add(methodLabel, BorderLayout.WEST);
        methodPanel.add(methodNameCombo, BorderLayout.CENTER);

        version = new JLabeledTextField(" 版本：", 7);

        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.add(classPanel, BorderLayout.WEST);
        webServerPanel.add(methodPanel, BorderLayout.CENTER);
        webServerPanel.add(version, BorderLayout.EAST);
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

    private List<String> getMethodNames(String interfaceCls) {
        if (!methodNames.containsKey(interfaceCls)) {
            List<String> method = new ArrayList();
            if (StringUtils.isNotEmpty(interfaceCls)) {
                try {
                    for (Method m : Class.forName(interfaceCls).getDeclaredMethods()) {
                        StringBuffer sb = new StringBuffer(m.getName()).append("(");
                        Class[] pts = m.getParameterTypes();
                        if (null != pts && pts.length > 0) {
                            for (Class pt : pts) {
                                sb.append(pt.getName()).append(",");
                            }
                            sb.deleteCharAt(sb.lastIndexOf(","));
                        }
                        sb.append(")");
                        method.add(sb.toString());
                    }
                } catch (ClassNotFoundException e) {
                    log.error("class {} not found!", interfaceCls);
                }
                methodNames.put(interfaceCls, method);
            }
        }
        return methodNames.get(interfaceCls);
    }

    private void setupMethods() {
        methodNameCombo.removeAllItems();
        if (null != classNameCombo.getSelectedItem()) {
            String className = classNameCombo.getSelectedItem().toString();
            if (className != null) {
                List<String> names = getMethodNames(className);
                for (String name : names) {
                    methodNameCombo.addItem(name);
                }
                methodNameCombo.repaint();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == classNameCombo) {
            setupMethods();
        }
    }


    private static class InterfaceFilter implements ClassFilter {
        private final String contains; // class name should contain this string
        private final String notContains; // class name should not contain this string
        private final ClassLoader contextClassLoader
                = Thread.currentThread().getContextClassLoader();

        InterfaceFilter(String contains, String notContains) {
            this.contains = contains;
            this.notContains = notContains;
        }

        @Override
        public boolean accept(String className) {
            if (contains != null && !className.contains(contains)) {
                return false; // It does not contain a required string
            }
            if (notContains != null && className.contains(notContains)) {
                return false; // It contains a banned string
            }
            try {
                return Class.forName(className, false, contextClassLoader).isInterface();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}