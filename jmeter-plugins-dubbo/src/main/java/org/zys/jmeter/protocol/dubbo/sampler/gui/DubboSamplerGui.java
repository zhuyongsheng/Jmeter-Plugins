package org.zys.jmeter.protocol.dubbo.sampler.gui;

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
import org.zys.jmeter.protocol.dubbo.sampler.DubboSampler;
import org.zys.jmeter.protocol.dubbo.sampler.util.DubboUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * Created by zhuyongsheng on 2018/3/26.
 */
public class DubboSamplerGui extends AbstractSamplerGui {

    private static final Logger log = LoggerFactory.getLogger(DubboSamplerGui.class);
    private static final String SAMPLER_TITLE = "DUBBO请求";

    private JLabeledTextField protocol;
    private JLabeledTextField host;
    private JLabeledTextField port;
    private JLabeledTextField version;
    private JLabeledTextField group;
    private JLabeledTextField cluster;
    private JLabeledChoice className;
    private JLabeledChoice methodName;
    private JTabbedPane tabbedPane;
    private ArgumentsPanel multiArgs;
    private JSyntaxTextArea singleArg;

    public DubboSamplerGui() {
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
        DubboSampler sampler = new DubboSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement testElement) {
        testElement.clear();
        configureTestElement(testElement);
        testElement.setProperty(DubboSampler.PROTOCOL, protocol.getText());
        testElement.setProperty(DubboSampler.HOST, host.getText());
        testElement.setProperty(DubboSampler.PORT, port.getText());
        testElement.setProperty(DubboSampler.CLASSNAME, className.getText());
        testElement.setProperty(DubboSampler.METHOD, methodName.getText());
        testElement.setProperty(DubboSampler.VERSION, version.getText());
        testElement.setProperty(DubboSampler.GROUP, group.getText());
        testElement.setProperty(DubboSampler.CLUSTER, cluster.getText());
        String paramTypeList = StringUtils.substringBetween(methodName.getText(), "(", ")");
        if (StringUtils.isNotEmpty(paramTypeList) && StringUtils.containsNone(paramTypeList, ",")) {
            Arguments arguments = new Arguments();
            arguments.addArgument(paramTypeList, singleArg.getText());
            testElement.setProperty(new TestElementProperty(DubboSampler.ARGUMENTS, arguments));
        } else {
            testElement.setProperty(new TestElementProperty(DubboSampler.ARGUMENTS, multiArgs.createTestElement()));
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
        protocol.setText(element.getPropertyAsString(DubboSampler.PROTOCOL));
        host.setText(element.getPropertyAsString(DubboSampler.HOST));
        port.setText(element.getPropertyAsString(DubboSampler.PORT));
        version.setText(element.getPropertyAsString(DubboSampler.VERSION));
        group.setText(element.getPropertyAsString(DubboSampler.GROUP));
        cluster.setText(element.getPropertyAsString(DubboSampler.CLUSTER));
        className.setText(element.getPropertyAsString(DubboSampler.CLASSNAME));
        methodName.setText(element.getPropertyAsString(DubboSampler.METHOD));
        Arguments arguments = (Arguments) element.getProperty(DubboSampler.ARGUMENTS).getObjectValue();
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
        cluster.setText("");
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
        requestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), SAMPLER_TITLE));
        requestPanel.add(createInterfacePanel(), BorderLayout.NORTH);
        requestPanel.add(createTabbedArgsPanel(), BorderLayout.CENTER);
        return requestPanel;
    }

    private JPanel createMultiArgsPanel() {
        multiArgs = new ArgumentsPanel(DubboSampler.ARGUMENTS);
        return multiArgs;
    }

    private JPanel createSingleArgPanel() {
        JLabel reqLabel = new JLabel(DubboSampler.ARGUMENTS); // $NON-NLS-1$
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
        className = new JLabeledChoice(DubboSampler.CLASSNAME, DubboUtils.getClassNames());
        methodName = new JLabeledChoice(DubboSampler.METHOD, ArrayUtils.EMPTY_STRING_ARRAY);
        className.addChangeListener((ChangeEvent evt) -> {
            if (evt.getSource() == className) {
                methodName.setValues(DubboUtils.getMethodNames(className.getText()));
            }
        });
        methodName.addChangeListener((ChangeEvent evt) -> {
            if (evt.getSource() == methodName) {
                setupArgs();
            }
        });
        version = new JLabeledTextField(DubboSampler.VERSION, 6);
        group = new JLabeledTextField(DubboSampler.GROUP, 6);
        cluster = new JLabeledTextField(DubboSampler.CLUSTER, 6);

        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.add(className);
        webServerPanel.add(methodName);
        webServerPanel.add(cluster);
        webServerPanel.add(version);
        webServerPanel.add(group);
        return webServerPanel;
    }

    private JPanel createDubboServerPanel() {
        protocol = new JLabeledTextField(DubboSampler.PROTOCOL, 4); // $NON-NLS-1$
        host = new JLabeledTextField(DubboSampler.HOST, 40); // $NON-NLS-1$
        port = new JLabeledTextField(DubboSampler.PORT, 7); // $NON-NLS-1$

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
                for (int i = 1; i <= paramTypes.length; i++) {
                    arguments.addArgument("var" + i + "(" + paramTypes[i] + ")", "");
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