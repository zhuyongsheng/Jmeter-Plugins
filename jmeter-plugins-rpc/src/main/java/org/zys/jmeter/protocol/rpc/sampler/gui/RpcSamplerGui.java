package org.zys.jmeter.protocol.rpc.sampler.gui;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.reflect.ClassFilter;
import org.apache.jorphan.reflect.ClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.rpc.sampler.RpcSampler;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuyongsheng on 2018/3/26.
 */
public class RpcSamplerGui extends AbstractSamplerGui implements ChangeListener {

    private static final Logger log = LoggerFactory.getLogger(RpcSamplerGui.class);

    private static Map<String, List<String>> methodNames = new HashMap<>();
    private static List<String> classNames = new ArrayList<>();

    private JLabeledTextField protocol;
    private JLabeledTextField host;
    private JLabeledTextField port;
    private JLabeledTextField version;
    private JLabeledTextField group;
    private JSyntaxTextArea args;
    private JLabeledChoice className;
    private JLabeledChoice methodName;


    private static final String[] SPATHS = new String[]{
            JMeterUtils.getJMeterHome() + "/lib/dubbo"             //需将/lib/dubbo加入user.classpath配置中，否则无法加载类
    };

    public RpcSamplerGui() {
        setupClassNames();
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
        group.setText(element.getPropertyAsString(RpcSampler.GROUP));
        className.setText(element.getPropertyAsString(RpcSampler.INTERFACE_CLASS));
        methodName.setText(element.getPropertyAsString(RpcSampler.METHOD));
        args.setInitialText(element.getPropertyAsString(RpcSampler.ARGS));
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
        className = new JLabeledChoice(" 接口:  ", classNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        className.addChangeListener(this);
        methodName = new JLabeledChoice(" 方法:  ", ArrayUtils.EMPTY_STRING_ARRAY);
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

    private void setupClassNames() {
        if (classNames.size() == 0) {
            try {
                classNames = ClassFinder.findClasses(SPATHS, new InterfaceFilter("Service", "RestService"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        if (evt.getSource() == className) {
            methodName.setValues(getMethodNames(className.getText()).toArray(ArrayUtils.EMPTY_STRING_ARRAY));
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