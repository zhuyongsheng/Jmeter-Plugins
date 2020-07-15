package org.zys.jmeter.protocol.grpc.sampler.gui;

/*
@Time : 2020/5/29 5:00 下午
@Author : yongshengzhu
*/

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
import org.zys.jmeter.protocol.grpc.sampler.GRpcSampler;
import org.zys.jmeter.protocol.grpc.sampler.util.GRpcUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class GRpcSamplerGui extends AbstractSamplerGui {

    private static final Logger log = LoggerFactory.getLogger(GRpcSamplerGui.class);
    private static final String SAMPLER_TITLE = "gRPC请求";

    private JLabeledTextField host;
    private JLabeledTextField port;
    private JLabeledChoice className;
    private JLabeledChoice methodName;
    private JTabbedPane tabbedPane;
    private ArgumentsPanel multiArgs;
    private JSyntaxTextArea singleArg;
    private JCheckBox secure;
    private JCheckBox closeChannel;

    public GRpcSamplerGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    public String getStaticLabel() {
        return SAMPLER_TITLE;
    }


    @Override
    public TestElement createTestElement() {
        GRpcSampler sampler = new GRpcSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement testElement) {
        testElement.clear();
        configureTestElement(testElement);
        testElement.setProperty(GRpcSampler.HOST, host.getText());
        testElement.setProperty(GRpcSampler.PORT, port.getText());
        testElement.setProperty(GRpcSampler.SECURE, secure.isSelected());
        testElement.setProperty(GRpcSampler.CLASS_NAME, className.getText());
        testElement.setProperty(GRpcSampler.METHOD, methodName.getText());
        String paramTypeList = StringUtils.substringBetween(methodName.getText(), "(", ")");
        if (StringUtils.isNotEmpty(paramTypeList) && StringUtils.containsNone(paramTypeList, ",")) {
            Arguments arguments = new Arguments();
            arguments.addArgument(paramTypeList, singleArg.getText());
            testElement.setProperty(new TestElementProperty(GRpcSampler.ARGUMENTS, arguments));
        } else {
            testElement.setProperty(new TestElementProperty(GRpcSampler.ARGUMENTS, multiArgs.createTestElement()));
        }
        testElement.setProperty(GRpcSampler.CLOSE_CHANNEL, closeChannel.isSelected());
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createGrpcServerPanel());
        box.add(createInterfacePanel());
        add(box, BorderLayout.NORTH);
        JPanel panel = createRequestPanel();
        add(panel, BorderLayout.CENTER);
        add(Box.createVerticalStrut(panel.getPreferredSize().height), BorderLayout.WEST);
    }

    public void configure(TestElement element) {
        super.configure(element);
        host.setText(element.getPropertyAsString(GRpcSampler.HOST));
        port.setText(element.getPropertyAsString(GRpcSampler.PORT));
        secure.setSelected(element.getPropertyAsBoolean(GRpcSampler.SECURE));
        className.setText(element.getPropertyAsString(GRpcSampler.CLASS_NAME));
        methodName.setText(element.getPropertyAsString(GRpcSampler.METHOD));
        Arguments arguments = (Arguments) element.getProperty(GRpcSampler.ARGUMENTS).getObjectValue();
        if (arguments.getArguments().size() == 1) {
            singleArg.setInitialText(arguments.getArgument(0).getValue());
        } else {
            multiArgs.configure(arguments);
        }
        closeChannel.setSelected(element.getPropertyAsBoolean(GRpcSampler.CLOSE_CHANNEL));
    }

    public void clearGui() {
        super.clearGui();
        host.setText("");
        port.setText("");
        secure.setSelected(false);
        className.setSelectedIndex(-1);
        methodName.setValues(ArrayUtils.EMPTY_STRING_ARRAY);
        methodName.setSelectedIndex(-1);
        multiArgs.clearGui();
        singleArg.setInitialText("");
        closeChannel.setSelected(false);
    }


    private JTabbedPane createTabbedArgsPanel() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(JMeterUtils.getResString("post_as_parameters"), createMultiArgsPanel());
        tabbedPane.addTab(JMeterUtils.getResString("post_body"), createSingleArgPanel());
        return tabbedPane;
    }

    private JPanel createRequestPanel() {
        JPanel requestPanel = new VerticalPanel();
        requestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "请求参数"));
        requestPanel.add(createTabbedArgsPanel(), BorderLayout.CENTER);
        return requestPanel;
    }

    private JPanel createMultiArgsPanel() {
        multiArgs = new ArgumentsPanel(true, GRpcSampler.ARGUMENTS);
        return multiArgs;
    }

    private JPanel createSingleArgPanel() {
        JLabel reqLabel = new JLabel(GRpcSampler.ARGUMENTS); // $NON-NLS-1$
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
        className = new JLabeledChoice(GRpcSampler.CLASS_NAME, GRpcUtils.getClassNames());
        methodName = new JLabeledChoice(GRpcSampler.METHOD, ArrayUtils.EMPTY_STRING_ARRAY);
        className.addChangeListener((ChangeEvent evt) -> {
            if (evt.getSource() == className) {
                methodName.setValues(GRpcUtils.getMethodNames(className.getText()));
            }
        });
        methodName.addChangeListener((ChangeEvent evt) -> {
            if (evt.getSource() == methodName) {
                setupArgs();
            }
        });

        closeChannel = new JCheckBox(GRpcSampler.CLOSE_CHANNEL);
        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "gRPC请求"));
        webServerPanel.add(className, BorderLayout.WEST);
        webServerPanel.add(methodName, BorderLayout.CENTER);
        webServerPanel.add(closeChannel,BorderLayout.EAST);
        return webServerPanel;
    }

    private JPanel createGrpcServerPanel() {
        host = new JLabeledTextField(GRpcSampler.HOST, 20); // $NON-NLS-1$
        port = new JLabeledTextField(GRpcSampler.PORT, 4); // $NON-NLS-1$
        secure = new JCheckBox(GRpcSampler.SECURE);
        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "服务器"));
        webServerPanel.add(host);
        webServerPanel.add(port);
        webServerPanel.add(secure);
//        webServerPanel.add(createInterfacePanel());
//        webServerPanel.add(closeChannel);
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