package org.zys.jmeter.protocol.kafka.sampler.gui;

import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.kafka.sampler.KafkaProducer;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 01369755 on 2018/3/28.
 */
public class KafkaProducerGui extends AbstractSamplerGui {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerGui.class);

    private JLabeledTextField topic = new JLabeledTextField("主题信息：");
    private JComboBox serializer = new JComboBox(KafkaProducer.SERIALIZE);
    private JSyntaxTextArea message;

    public KafkaProducerGui() {
        init();
    }

    private void init(){
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        add(box, BorderLayout.NORTH);
        JPanel panel = creatMessagePanel();
        add(panel, BorderLayout.CENTER);
        add(Box.createVerticalStrut(panel.getPreferredSize().height), BorderLayout.WEST);

    }

    public void configure(TestElement element) {

        super.configure(element);
        topic.setText(element.getPropertyAsString(KafkaProducer.TOPIC));
        serializer.setSelectedItem(element.getPropertyAsString(KafkaProducer.SERIALIZER));
        message.setInitialText(element.getPropertyAsString(KafkaProducer.MESSAGE));
//        args.setCaretPosition(0);
    }

    private JPanel creatMessagePanel() {
        JPanel messagePanel = new VerticalPanel();
        messagePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "消息信息"));
        messagePanel.add(topic);
        messagePanel.add(createLabelPanel("序列化类：", serializer));
        messagePanel.add(createArgsPanel(),BorderLayout.CENTER);
        return messagePanel;
    }

    private JPanel createArgsPanel() {
        message = JSyntaxTextArea.getInstance(20, 20);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(makeBorder());
        panel.add(JTextScrollPane.getInstance(message), BorderLayout.CENTER);
        return panel;
    }


    public String getStaticLabel() {
        return "KAFKA生产者";
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    private JPanel createLabelPanel(String name, JComponent field) {
        JPanel addInnerPanel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(name);
        label.setLabelFor(field);
        addInnerPanel.add(label, BorderLayout.WEST);
        addInnerPanel.add(field, BorderLayout.CENTER);
        return addInnerPanel;
    }
    @Override
    public TestElement createTestElement() {
        KafkaProducer sampler = new KafkaProducer();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        element.clear();
        configureTestElement(element);
        element.setProperty(KafkaProducer.TOPIC, topic.getText());
        element.setProperty(KafkaProducer.SERIALIZER, serializer.getSelectedItem().toString());
        element.setProperty(KafkaProducer.MESSAGE, message.getText());
    }

    public void clearGui() {
        super.clearGui();
        topic.setText("");
        serializer.setSelectedIndex(0);
        message.setInitialText("");
        message.setCaretPosition(0);
    }
}
