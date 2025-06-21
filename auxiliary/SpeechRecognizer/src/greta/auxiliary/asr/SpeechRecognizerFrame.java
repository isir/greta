/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.auxiliary.asr;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SpeechRecognizerFrame extends JFrame implements SpeechRecognizerListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1584938014933004656L;
	
	private JLabel connectedLabel;
	private JTextField hostTextField;
	private JTextField portTextField;
	private JTextField requestTopicTextField;
	private JTextField responseTopicTextField;
	private JButton resetConnectionButton;
	private ASRMain asrMain= null;
	private ActiveMQConnector groupBehaviorMain = null;
	 private SpeechRecognizer sr = null;
	public SpeechRecognizerFrame() {
		this.initComponents();
	}
	
	private void initComponents() {
		Container contentPane = this.getContentPane();
		
		JPanel overallPanel = new JPanel();
		contentPane.add(overallPanel);
		
		overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.Y_AXIS));
		
		JPanel connectedPanel = new JPanel();
		connectedPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		connectedPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.connectedLabel = new JLabel("");
		connectedPanel.add(this.connectedLabel);
		this.setConnectionLabel(false);
		overallPanel.add(connectedPanel);
		
		JPanel hostPanel = new JPanel();
		hostPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		hostPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		hostPanel.add(new JLabel("Host"));
		this.hostTextField = new JTextField("", 50);
		hostPanel.add(this.hostTextField);
		overallPanel.add(hostPanel);
		
		JPanel portPanel = new JPanel();
		portPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		portPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		portPanel.add(new JLabel("Port"));
		this.portTextField = new JTextField("", 50);
		portPanel.add(this.portTextField);
		overallPanel.add(portPanel);
		
		JPanel requesttopicPanel = new JPanel();
		requesttopicPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		requesttopicPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		requesttopicPanel.add(new JLabel("Request Topic"));
		this.requestTopicTextField = new JTextField("", 50);
		requesttopicPanel.add(this.requestTopicTextField);
		overallPanel.add(requesttopicPanel);
		
		JPanel responseTopicPanel = new JPanel();
		responseTopicPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		responseTopicPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		responseTopicPanel.add(new JLabel("Response Topic"));
		this.responseTopicTextField = new JTextField("", 50);
		responseTopicPanel.add(this.responseTopicTextField);
		overallPanel.add(responseTopicPanel);
		
		JPanel responseConnectionButtonPanel = new JPanel();
		responseConnectionButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		responseConnectionButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.resetConnectionButton = new JButton("Reset Connection");
		this.resetConnectionButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		responseConnectionButtonPanel.add(this.resetConnectionButton);
		overallPanel.add(responseConnectionButtonPanel);
		this.resetConnectionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SpeechRecognizerFrame.this.establishActiveMqConnection();
			}
			
		});
                

	}
	
	private void setConnectionLabel(boolean status) {
		if (status) {
			this.connectedLabel.setText("Connected");
			this.connectedLabel.setForeground(Color.GREEN);
		} else {
			this.connectedLabel.setText("Not connected");
			this.connectedLabel.setForeground(Color.RED);
		}
	}
	
	private void establishActiveMqConnection() {
		String host = this.hostTextField.getText().trim();
		String port = this.portTextField.getText().trim();
		String requestTopic = this.requestTopicTextField.getText().trim();
		String responseTopic = this.responseTopicTextField.getText().trim();
                
                asrMain.establishActiveMqConnection(host, port, requestTopic, responseTopic);
		
		
		this.asrMain.getActiveMQConnector().registerAsSpeechRecognizerListener(this);
		this.setConnectionLabel(false);
		
	}

	public void setGroupBehaviorMain(ASRMain asrMain) {
		this.asrMain = asrMain;
		
		if (this.asrMain.getHost() != null) {
			this.hostTextField.setText(this.asrMain.getHost());
		}
		if (this.asrMain.getPort() != null) {
			this.portTextField.setText(this.asrMain.getPort());
		}
		if (this.asrMain.getRequestTopic() != null) {
			this.requestTopicTextField.setText(this.asrMain.getRequestTopic());
		}
		if (this.asrMain.getResponseTopic() != null) {
			this.responseTopicTextField.setText(this.asrMain.getResponseTopic());
		}
		SpeechRecognizerFrame.this.establishActiveMqConnection();
                asrMain.initialiseSpeechRecognition();
	}

	@Override
	public void onConnectionStarted() {
		this.setConnectionLabel(true);
	}
}
