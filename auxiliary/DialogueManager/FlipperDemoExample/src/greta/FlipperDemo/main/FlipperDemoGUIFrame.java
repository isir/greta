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
package greta.FlipperDemo.main;

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

public class FlipperDemoGUIFrame extends JFrame  {

	/**
	 * 
	 */

	
	private JLabel connectedLabel;
	private JTextField hostTextField;
	private JTextField portTextField;
	private JTextField requestTopicTextField;
	private JTextField responseTopicTextField;
        private JTextField flipperPropertyResTextField;
        private JTextField flipperTemplateFolderTextField;
	private JButton resetConnectionButton;
	private FlipperLauncherMain flipperDemoMain= null;
 
      //private ActiveMQConnector groupBehaviorMain = null;
      //	 private SpeechRecognizer sr = null;
	public FlipperDemoGUIFrame() {
		this.initComponents();
	}
	
	private void initComponents() {
		Container contentPane = this.getContentPane();
		
		JPanel overallPanel = new JPanel();
		contentPane.add(overallPanel);
		
		overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.Y_AXIS));
		
		/*JPanel connectedPanel = new JPanel();
		connectedPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		connectedPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.connectedLabel = new JLabel("");
		connectedPanel.add(this.connectedLabel);
		this.setConnectionLabel(false);
		overallPanel.add(connectedPanel);
		*/
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
                
                JPanel flipperPropertyPanel = new JPanel();
		flipperPropertyPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		flipperPropertyPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		flipperPropertyPanel.add(new JLabel("Flipper Property File"));
		this.flipperPropertyResTextField = new JTextField("", 50);
		flipperPropertyPanel.add(this.flipperPropertyResTextField);
		overallPanel.add(flipperPropertyPanel);
                
                JPanel templateFolderPanel = new JPanel();
		templateFolderPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		templateFolderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		templateFolderPanel.add(new JLabel("Flipper Template Folder"));
		this.flipperTemplateFolderTextField = new JTextField("", 50);
		templateFolderPanel.add(this.flipperTemplateFolderTextField);
		overallPanel.add(templateFolderPanel);
                               
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
				FlipperDemoGUIFrame.this.setActiveMqParameters();
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
	
	private void setActiveMqParameters() {
		String host = this.hostTextField.getText().trim();
		String port = this.portTextField.getText().trim();
		String requestTopic = this.requestTopicTextField.getText().trim();
		String responseTopic = this.responseTopicTextField.getText().trim();
                
                flipperDemoMain.setActiveMqParameters(host, port, requestTopic, responseTopic);
		//this.setConnectionLabel(false);
		
	}


	public void setFlipperDemoMain(FlipperLauncherMain flipperDemoMain) {
                //this.flipperDemoMain = flipperDemoMain;
		this.flipperDemoMain = FlipperLauncherMain.getInstance();
		
		if (this.flipperDemoMain.getHost() != null) {
			this.hostTextField.setText(this.flipperDemoMain.getHost());
		}
		if (this.flipperDemoMain.getPort() != null) {
			this.portTextField.setText(this.flipperDemoMain.getPort());
		}
		if (this.flipperDemoMain.getGretaASRTopic()!= null) {
			this.requestTopicTextField.setText(this.flipperDemoMain.getGretaASRTopic());
		}
		if (this.flipperDemoMain.getGretaInputTopic()!= null) {
			this.responseTopicTextField.setText(this.flipperDemoMain.getGretaInputTopic());
		}
                if (this.flipperDemoMain.getFlipperPropertyResource()!= null) {
			this.flipperPropertyResTextField.setText(this.flipperDemoMain.getFlipperPropertyResource());
		}
                if (this.flipperDemoMain.getflipperTemplateFolderPath()!= null) {
			this.flipperTemplateFolderTextField.setText(this.flipperDemoMain.getflipperTemplateFolderPath());
		}

		FlipperDemoGUIFrame.this.setActiveMqParameters();
                flipperDemoMain.init();
	}

	
	public void onConnectionStarted() {
		this.setConnectionLabel(true);
	}
}
