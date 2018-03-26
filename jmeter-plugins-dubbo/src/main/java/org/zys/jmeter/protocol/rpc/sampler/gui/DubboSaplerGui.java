package org.zys.jmeter.protocol.rpc.sampler.gui;

import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.zys.jmeter.protocol.rpc.sampler.DubboSampler;
import org.zys.jmeter.protocol.rpc.sampler.config.DubboConfig;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 01369755 on 2018/3/26.
 */
public class DubboSaplerGui extends AbstractSamplerGui {


    private JTextField serviceName = new JTextField();
    private JComboBox<String> method;
    private JSyntaxTextArea args;

    public DubboSaplerGui()
    {
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
        DubboSampler sampler = new DubboSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement testElement) {

    }

    private void init()
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createLabelPanel("服务名称：",serviceName));
        box.add(createLabelPanel("方法名称：",method));
        JPanel panel = createArgsPanel();
        add(panel);
        add(Box.createVerticalStrut(panel.getPreferredSize().height), BorderLayout.WEST);
    }

    private JPanel createArgsPanel() {
        this.args = JSyntaxTextArea.getInstance(20, 20);

        JLabel label = new JLabel("请求参数:");
        label.setLabelFor(this.args);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(JTextScrollPane.getInstance(this.args), BorderLayout.CENTER);

/*        JTextArea explain = new JTextArea("请在参数区域输入对应参数");
        explain.setLineWrap(true);
        explain.setEditable(false);
        explain.setBackground(getBackground());
        panel.add(explain, "South");*/

        return panel;
    }
//    private JPanel createServiceNamePanel() {
//        JPanel addInnerPanel = new JPanel(new BorderLayout(5, 0));
//        JLabel label = new JLabel("服务名称:");
//        label.setLabelFor(serviceName);
//        addInnerPanel.add(label, BorderLayout.WEST);
//        addInnerPanel.add(serviceName, BorderLayout.CENTER);
//        return addInnerPanel;
//    }

    private JPanel createLabelPanel(String name, JComponent component) {
        JPanel addInnerPanel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(name);
        label.setLabelFor(component);
//        method.setSelectedItem(labels[0]);
        addInnerPanel.add(label, BorderLayout.WEST);
        addInnerPanel.add(component, BorderLayout.CENTER);
        return addInnerPanel;
    }
}
