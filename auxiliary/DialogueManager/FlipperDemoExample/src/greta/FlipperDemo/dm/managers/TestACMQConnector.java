/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.dm.managers;

import eu.aria.util.activemq.ActiveMQStatusGUI;
import eu.aria.util.activemq.ActiveMQWrapper;
import eu.aria.util.activemq.IMessageReceiver;
import eu.aria.util.activemq.SimpleProducerWrapper;
import eu.aria.util.activemq.SimpleReceiverWrapper;
import eu.aria.util.activemq.util.UrlBuilder;
import eu.aria.util.translator.Translator;
import eu.aria.util.translator.api.AgentFeedback;
import java.util.HashSet;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public class TestACMQConnector {
  public static final String AMQ_PROPERTY_ID = "content-id";
  
  private ActiveMQStatusGUI senderGui;
  
  private ActiveMQStatusGUI feedbackGui;
  
  private SimpleProducerWrapper fmlSender;
  
  private SimpleReceiverWrapper feedbackReceiver;
  
  private TestReplGroup replacerGroup;
  
  private HashSet<TestACMQListener> listeners = new HashSet<>();
  
  private Translator.XMLListener xmlListener = this::onFML;
  
  private int fmlId = 0;
  
  public void setReplacerGroup(TestReplGroup replacerGroup) {
    if (this.replacerGroup != null)
      this.replacerGroup.removeXMLListener(this.xmlListener); 
    this.replacerGroup = replacerGroup;
    replacerGroup.addXMLListener(this.xmlListener, true);
  }
  
  public void initialiseSender(String host, String port, String topic) {
    initialiseSender(UrlBuilder.getUrlTcp(host, port), topic);
  }
  
  public void initialiseSender(String url, String topic) {
    this.fmlSender = new SimpleProducerWrapper(url, topic, true);
    this.fmlSender.init();
    if (this.senderGui != null)
      this.senderGui.setActiveMQWrapper((ActiveMQWrapper)this.fmlSender); 
  }
  
  public void initialiseFeedback(String host, String port, String topic) {
    initialiseFeedback(UrlBuilder.getUrlTcp(host, port), topic);
  }
  
  public void initialiseFeedback(String url, String topic) {
    this.feedbackReceiver = new SimpleReceiverWrapper(url, topic, true);
    this.feedbackReceiver.start((Message message) -> {
          AgentFeedback feedback = AgentFeedback.FromJMSMessage(message);
          if (feedback != null)
            this.listeners.forEach((t)->{});
        });
    if (this.feedbackGui != null)
      this.feedbackGui.setActiveMQWrapper((ActiveMQWrapper)this.feedbackReceiver); 
  }
  
  public void close() {
    this.fmlSender.close();
    this.feedbackReceiver.stop();
  }
  
  private void onFML(String fml, String id) {
    if (this.fmlSender != null) {
      TextMessage textMessage = this.fmlSender.createTextMessage(fml);
      if (textMessage != null) {
        if (id == null)
          id = "id_" + this.fmlId++; 
        try {
          textMessage.setStringProperty("content-id", id);
        } catch (JMSException e) {
          e.printStackTrace();
        } 
        this.fmlSender.sendMessage((Message)textMessage);
      } else {
        System.err.println("Could not send fml! Please check ActiveMQ connection!");
      } 
    } 
  }
  
  public void addFeedbackListener(TestACMQListener listener) {
    this.listeners.add(listener);
  }
  
  public void removeFeedbackListener(TestACMQListener listener) {
    this.listeners.remove(listener);
  }
  
  public void showSenderGui() {
    showSenderGui(200, 200);
  }
  
  public void showSenderGui(int x, int y) {
    if (this.senderGui != null) {
      if (!this.senderGui.isVisible())
        this.senderGui.show(x, y); 
      return;
    } 
    this.senderGui = new ActiveMQStatusGUI();
    if (this.fmlSender != null)
      this.senderGui.setActiveMQWrapper((ActiveMQWrapper)this.fmlSender); 
    this.senderGui.show(x, y);
  }
  
  public void showFeedbackGui() {
    showFeedbackGui(200, 200);
  }
  
  public void showFeedbackGui(int x, int y) {
    if (this.feedbackGui != null) {
      if (!this.feedbackGui.isVisible())
        this.feedbackGui.show(x, y); 
      return;
    } 
    this.feedbackGui = new ActiveMQStatusGUI();
    if (this.feedbackReceiver != null)
      this.feedbackGui.setActiveMQWrapper((ActiveMQWrapper)this.feedbackReceiver); 
    this.feedbackGui.show(x, y);
  }
}

