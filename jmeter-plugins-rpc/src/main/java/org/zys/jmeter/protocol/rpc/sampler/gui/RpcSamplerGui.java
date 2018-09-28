package org.zys.jmeter.protocol.rpc.sampler.gui;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
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
    private JTabbedPane tabbedPane;
    private ArgumentsPanel multiArgs;
    private JSyntaxTextArea singleArg;

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
        testElement.setProperty(RpcSampler.CLASSNAME, className.getText());
        testElement.setProperty(RpcSampler.METHOD, methodName.getText());
        testElement.setProperty(RpcSampler.VERSION, version.getText());
        testElement.setProperty(RpcSampler.GROUP, group.getText());
        String paramTypeList = StringUtils.substringBetween(methodName.getText(), "(", ")");
        if (StringUtils.isNotEmpty(paramTypeList) && StringUtils.containsNone(paramTypeList, ",")) {
            Arguments arguments = new Arguments();
            arguments.addArgument(paramTypeList, singleArg.getText());
            testElement.setProperty(new TestElementProperty(RpcSampler.ARGUMENTS, arguments));
        } else {
            testElement.setProperty(new TestElementProperty(RpcSampler.ARGUMENTS, multiArgs.createTestElement()));
        }
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createDubboServerPanel());
        add(box, BorderLayout.NORTH);
        JPanel panel = createRequestPanel();
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
        className.setText(element.getPropertyAsString(RpcSampler.CLASSNAME));
        methodName.setText(element.getPropertyAsString(RpcSampler.METHOD));
        Arguments arguments = (Arguments) element.getProperty(RpcSampler.ARGUMENTS).getObjectValue();
        if (arguments.getArguments().size() == 1) {
            singleArg.setInitialText(arguments.getArgument(0).getValue());
        } else {
            multiArgs.configure(arguments);
        }
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
        multiArgs.clearGui();
        singleArg.setInitialText("");
    }


    private JTabbedPane createTabbedArgsPanel() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(JMeterUtils.getResString("post_as_parameters"), createMultiArgsPanel());
        tabbedPane.addTab(JMeterUtils.getResString("post_body"), createSingleArgPanel());
        return tabbedPane;
    }

    private JPanel createRequestPanel() {
        JPanel requestPanel = new VerticalPanel();
        requestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dubbo请求"));
        requestPanel.add(createInterfacePanel(), BorderLayout.NORTH);
        requestPanel.add(createTabbedArgsPanel(), BorderLayout.CENTER);
        return requestPanel;
    }

    private JPanel createMultiArgsPanel() {
        multiArgs = new ArgumentsPanel(true, RpcSampler.ARGUMENTS);
        return multiArgs;
    }

    private JPanel createSingleArgPanel() {
        JLabel reqLabel = new JLabel(RpcSampler.ARGUMENTS); // $NON-NLS-1$
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        labelPanel.add(reqLabel);
        singleArg = JSyntaxTextArea.getInstance(15, 80);
        singleArg.setLanguage("java"); //$NON-NLS-1$
        reqLabel.setLabelFor(singleArg);

        JPanel reqDataPanel = new JPanel(new BorderLayout(5, 0));
        reqDataPanel.add(labelPanel, BorderLayout.NORTH);
        reqDataPanel.add(JTextScrollPane.getInstance(singleArg), BorderLayout.CENTER);
        return reqDataPanel;
    }

    private JPanel createInterfacePanel() {
        className = new JLabeledChoice(RpcSampler.CLASSNAME, RpcUtils.getClassNames());
        methodName = new JLabeledChoice(RpcSampler.METHOD, ArrayUtils.EMPTY_STRING_ARRAY);
        className.addChangeListener((ChangeEvent evt) -> {
            if (evt.getSource() == className) {
                methodName.setValues(RpcUtils.getMethodNames(className.getText()));
            }
        });
        methodName.addChangeListener((ChangeEvent evt) -> {
            if (evt.getSource() == methodName) {
                setupArgs();
            }
        });
        version = new JLabeledTextField(RpcSampler.VERSION, 6);
        group = new JLabeledTextField(RpcSampler.GROUP, 6);

        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.add(className);
        webServerPanel.add(methodName);
        webServerPanel.add(version);
        webServerPanel.add(group);
        return webServerPanel;
    }

    private JPanel createDubboServerPanel() {
        protocol = new JLabeledTextField(RpcSampler.PROTOCOL, 4); // $NON-NLS-1$
        host = new JLabeledTextField(RpcSampler.HOST, 40); // $NON-NLS-1$
        port = new JLabeledTextField(RpcSampler.PORT, 7); // $NON-NLS-1$

        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "服务信息"));
        webServerPanel.add(protocol);
        webServerPanel.add(host);
        webServerPanel.add(port);
        return webServerPanel;
    }

    private void setupArgs() {
        String paramTypeList = StringUtils.substringBetween(methodName.getText(), "(", ")");
        if (StringUtils.isNotEmpty(paramTypeList)) {
            String[] paramTypes = paramTypeList.split(",");
            if (1 == paramTypes.length) {
                tabbedPane.setEnabledAt(0, false);
                tabbedPane.setEnabledAt(1, true);
                tabbedPane.setSelectedIndex(1);
                multiArgs.clearGui();
            } else {
                tabbedPane.setSelectedIndex(0);
                tabbedPane.setEnabledAt(0, true);
                tabbedPane.setEnabledAt(1, false);
                singleArg.setInitialText("");
                Arguments arguments = new Arguments();
                for (String paramType : paramTypes) {
                    arguments.addArgument(paramType, "");
                }
                multiArgs.configure(arguments);
            }
        } else {
            tabbedPane.setSelectedIndex(0);
            tabbedPane.setEnabledAt(0, false);
            tabbedPane.setEnabledAt(1, false);
            singleArg.setInitialText("");
            multiArgs.clearGui();
        }
    }
}