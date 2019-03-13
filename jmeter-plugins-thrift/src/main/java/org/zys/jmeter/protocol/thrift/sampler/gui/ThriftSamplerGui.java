package org.zys.jmeter.protocol.thrift.sampler.gui;

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
import org.zys.jmeter.protocol.thrift.sampler.ThriftSampler;
import org.zys.jmeter.protocol.thrift.sampler.util.ThriftUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * Created by zhuyongsheng on 2018/3/26.
 */
public class ThriftSamplerGui extends AbstractSamplerGui {

    private static final Logger log = LoggerFactory.getLogger(ThriftSamplerGui.class);
    private static final String SAMPLER_TITLE = "Thrift请求";

    private JLabeledTextField host;
    private JLabeledTextField port;
    private JLabeledChoice className;
    private JLabeledChoice methodName;
    private JTabbedPane tabbedPane;
    private ArgumentsPanel multiArgs;
    private JSyntaxTextArea singleArg;

    public ThriftSamplerGui() {
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
        ThriftSampler sampler = new ThriftSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement testElement) {
        testElement.clear();
        configureTestElement(testElement);
        testElement.setProperty(ThriftSampler.HOST, host.getText());
        testElement.setProperty(ThriftSampler.PORT, port.getText());
        testElement.setProperty(ThriftSampler.CLASSNAME, className.getText());
        testElement.setProperty(ThriftSampler.METHOD, methodName.getText());
        String paramTypeList = StringUtils.substringBetween(methodName.getText(), "(", ")");
        if (StringUtils.isNotEmpty(paramTypeList) && StringUtils.containsNone(paramTypeList, ",")) {
            Arguments arguments = new Arguments();
            arguments.addArgument(paramTypeList, singleArg.getText());
            testElement.setProperty(new TestElementProperty(ThriftSampler.ARGUMENTS, arguments));
        } else {
            testElement.setProperty(new TestElementProperty(ThriftSampler.ARGUMENTS, multiArgs.createTestElement()));
        }
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createThriftServerPanel());
        add(box, BorderLayout.NORTH);
        JPanel panel = createRequestPanel();
        add(panel, BorderLayout.CENTER);
        add(Box.createVerticalStrut(panel.getPreferredSize().height), BorderLayout.WEST);
    }

    public void configure(TestElement element) {
        super.configure(element);
        host.setText(element.getPropertyAsString(ThriftSampler.HOST));
        port.setText(element.getPropertyAsString(ThriftSampler.PORT));
        className.setText(element.getPropertyAsString(ThriftSampler.CLASSNAME));
        methodName.setText(element.getPropertyAsString(ThriftSampler.METHOD));
        Arguments arguments = (Arguments) element.getProperty(ThriftSampler.ARGUMENTS).getObjectValue();
        if (arguments.getArguments().size() == 1) {
            singleArg.setInitialText(arguments.getArgument(0).getValue());
        } else {
            multiArgs.configure(arguments);
        }
    }

    public void clearGui() {
        super.clearGui();
        host.setText("");
        port.setText("");
        className.setSelectedIndex(-1);
        methodName.setValues(ArrayUtils.EMPTY_STRING_ARRAY);
        methodName.setSelectedIndex(-1);
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
        requestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "请求参数"));
        requestPanel.add(createTabbedArgsPanel(), BorderLayout.CENTER);
        return requestPanel;
    }

    private JPanel createMultiArgsPanel() {
        multiArgs = new ArgumentsPanel(true, ThriftSampler.ARGUMENTS);
        return multiArgs;
    }

    private JPanel createSingleArgPanel() {
        JLabel reqLabel = new JLabel(ThriftSampler.ARGUMENTS); // $NON-NLS-1$
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
        className = new JLabeledChoice(ThriftSampler.CLASSNAME, ThriftUtils.getClassNames());
        methodName = new JLabeledChoice(ThriftSampler.METHOD, ArrayUtils.EMPTY_STRING_ARRAY);
        className.addChangeListener((ChangeEvent evt) -> {
            if (evt.getSource() == className) {
                methodName.setValues(ThriftUtils.getMethodNames(className.getText()));
            }
        });
        methodName.addChangeListener((ChangeEvent evt) -> {
            if (evt.getSource() == methodName) {
                setupArgs();
            }
        });

        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.add(className, BorderLayout.WEST);
        webServerPanel.add(methodName,BorderLayout.CENTER);
        return webServerPanel;
    }

    private JPanel createThriftServerPanel() {
        host = new JLabeledTextField(ThriftSampler.HOST, 4); // $NON-NLS-1$
        port = new JLabeledTextField(ThriftSampler.PORT, 4); // $NON-NLS-1$
        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "服务信息"));
        webServerPanel.add(host);
        webServerPanel.add(port);
        webServerPanel.add(createInterfacePanel());
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