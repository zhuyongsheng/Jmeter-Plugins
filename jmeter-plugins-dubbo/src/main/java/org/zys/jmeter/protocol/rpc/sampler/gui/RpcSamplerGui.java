package org.zys.jmeter.protocol.rpc.sampler.gui;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import java.awt.BorderLayout;
import java.lang.reflect.Method;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.rpc.sampler.RpcSampler;

/**
 * Created by 01369755 on 2018/3/14.
 */
public class RpcSamplerGui extends AbstractSamplerGui {
    private static final Logger log = LoggerFactory.getLogger(RpcSamplerGui.class);

    private JTextField zkAddr = new JTextField(20);
    private JTextField interfaceName = new JTextField(40);
    private JTextField version = new JTextField(8);
    private JComboBox<String> method = new JComboBox();
    private JSyntaxTextArea args;

    public RpcSamplerGui()
    {
        init();
    }

    public String getLabelResource() {
        return null;
    }

    public String getStaticLabel() {
        return RpcSampler.NAME;
    }

    public void configure(TestElement element) {
        this.zkAddr.setText(element.getPropertyAsString(RpcSampler.ZOOKEEPER));
        this.interfaceName.setText(element.getPropertyAsString(RpcSampler.INTERFACE));
        this.version.setText(element.getPropertyAsString(RpcSampler.VERSION));
        this.method.removeAllItems();
        if (null != interfaceName && interfaceName.getText().length() != 0) {
            try {
                Class interfaceClass = Class.forName(interfaceName.getText());
                Method[] methods = interfaceClass.getMethods();
                for (Method m : methods){
                    method.addItem(m.getName());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            method.setSelectedItem(element.getPropertyAsString(RpcSampler.METHOD_NAME));
        }
        super.configure(element);
        args.setInitialText(element.getPropertyAsString(RpcSampler.ARGS));
        args.setCaretPosition(0);
    }

    public TestElement createTestElement()
    {
        RpcSampler sampler = new RpcSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /*private JPanel createAreaPanel(String name, JSyntaxTextArea aera) {
        JLabel reqLabel = new JLabel(name);
        aera = JSyntaxTextArea.getInstance(30, 80);
        aera.setLanguage("text");
        reqLabel.setLabelFor(aera);

        JPanel reqDataPanel = new JPanel(new BorderLayout(5, 0));
        reqDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));

        reqDataPanel.add(reqLabel, "West");
        reqDataPanel.add(JTextScrollPane.getInstance(aera), "Center");
        return reqDataPanel;
    }*/

    private JPanel createArgsPanel() {
        this.args = JSyntaxTextArea.getInstance(20, 20);

        JLabel label = new JLabel(RpcSampler.ARGS + ":");
        label.setLabelFor(this.args);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, "North");
        panel.add(JTextScrollPane.getInstance(this.args), "Center");

        JTextArea explain = new JTextArea("请在参数区域输入对应参数");
        explain.setLineWrap(true);
        explain.setEditable(false);
        explain.setBackground(getBackground());
        panel.add(explain, "South");

        return panel;
    }

    public void modifyTestElement(TestElement te) {
        te.clear();
        configureTestElement(te);
        te.setProperty(RpcSampler.ZOOKEEPER, this.zkAddr.getText());
        te.setProperty(RpcSampler.INTERFACE, this.interfaceName.getText());
        te.setProperty(RpcSampler.VERSION, this.version.getText());
        if (this.method.getSelectedItem() != null) {
            te.setProperty(RpcSampler.METHOD_NAME, this.method.getSelectedItem().toString());
            te.setProperty(RpcSampler.METHOD_INDEX, this.method.getSelectedIndex());
        }
        te.setProperty(RpcSampler.ARGS, this.args.getText());
    }

    private void init()
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createLabelPanel(RpcSampler.ZOOKEEPER + ":", this.zkAddr));
        box.add(createInterfacePanel());
        add(box, "North");
        JPanel panel = createArgsPanel();
        add(createArgsPanel());
        add(Box.createVerticalStrut(panel.getPreferredSize().height), "West");
    }

    private JPanel createInterfacePanel()
    {
        JPanel addInnerPanel = new JPanel(new BorderLayout(5, 0));
        addInnerPanel.add(createLabelPanel(RpcSampler.INTERFACE + ":", this.interfaceName), "West");
        addInnerPanel.add(createMethodPanel(), "Center");
        addInnerPanel.add(createLabelPanel(RpcSampler.VERSION + ":", this.version), "East");
        return addInnerPanel;
    }

    private JPanel createLabelPanel(String name, JTextField field) {
        JPanel addInnerPanel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(name);
        label.setLabelFor(field);
        addInnerPanel.add(label, "West");
        addInnerPanel.add(field, "Center");
        return addInnerPanel;
    }

    private JPanel createMethodPanel()
    {
        JPanel addInnerPanel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(RpcSampler.METHOD_NAME + ":");
        label.setLabelFor(this.method);
        addInnerPanel.add(label, "West");
        addInnerPanel.add(this.method, "Center");
        return addInnerPanel;
    }

    public void clearGui() {
        super.clearGui();
        this.zkAddr.setText("");
        this.interfaceName.setText("");
        this.version.setText("");
        this.method.removeAllItems();
        this.args.setInitialText("");
    }
}