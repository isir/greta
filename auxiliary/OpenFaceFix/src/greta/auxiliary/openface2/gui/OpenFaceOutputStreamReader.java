/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.openface2.gui;

import greta.auxiliary.openface2.OpenFaceOutputStreamCSVReader;
import greta.auxiliary.openface2.OpenFaceOutputStreamZeroMQReader;
import greta.auxiliary.openface2.util.StringArrayListener;
import greta.auxiliary.zeromq.ConnectionListener;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.keyframes.face.AUEmitter;
import greta.core.keyframes.face.AUEmitterImpl;
import greta.core.keyframes.face.AUPerformer;
import greta.core.repositories.AUAPFrame;
import greta.core.util.IniManager;
import greta.core.util.id.ID;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import com.illposed.osc.*;
import com.illposed.osc.transport.udp.OSCPort;
import com.illposed.osc.transport.udp.OSCPortOut;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;

/**
 *
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 * @author Brice Donval
 */
public class OpenFaceOutputStreamReader extends javax.swing.JFrame implements AUEmitter, BAPFrameEmitter, ConnectionListener, StringArrayListener {

    private static final Logger LOGGER = Logger.getLogger(OpenFaceOutputStreamReader.class.getName());

    private static final Color green = new Color(0, 150, 0);
    private static final Color red = Color.RED;

    private static final String fileProperty = "GUI.file";
    private static final String statusProperty = "word.status";
    private static final String connectedProperty = "network.connected";
    private static final String notConnectedProperty = "network.notconnected";
    private static final String hostProperty = "network.host";
    private static final String portProperty = "network.port";

    private String actualConnectedProperty;
    // OSC is use to monitor AUs signal, mainly for debug
    protected boolean useOSC = false;
    protected OSCPortOut oscOut = null;
    protected int oscPort = OSCPort.defaultSCOSCPort();    

    private AUEmitterImpl auEmitter = new AUEmitterImpl();
    private BAPFrameEmitterImpl bapFrameEmitter = new BAPFrameEmitterImpl();

    private OpenFaceOutputStreamCSVReader csvReader = new OpenFaceOutputStreamCSVReader(this);
    private OpenFaceOutputStreamZeroMQReader zeroMQReader = new OpenFaceOutputStreamZeroMQReader(this);

     
    
    /**
     * Creates new form OpenFaceOutputStreamReader
     */
    public OpenFaceOutputStreamReader() {
        initComponents();
        
        jSpinnerfilterPow.setModel(new SpinnerNumberModel(0.0,0.0,10.0,0.1));
        jSpinnerfilterMaxQueueSize.setValue(getFilterMaxQueueSize());
        setConnected(false);
    }
    /*
    FILTERS
    */
    // We assume both reader use the same filter size
    public int getFilterMaxQueueSize(){
        return zeroMQReader.getFilterMaxQueueSize();
    }
    
    // We assume both reader use the same filter size
    public void setFilterMaxQueueSize(int value){
        zeroMQReader.setFilterMaxQueueSize(value);
        csvReader.setFilterMaxQueueSize(value);
    }
    
    // We assume both reader use the same filter pow
    public int getFilterPow(){
        return zeroMQReader.getFilterMaxQueueSize();
    }
    
    // We assume both reader use the same parameters
    public void setFilterPow(double i){
        zeroMQReader.setFilterPow(i);
        csvReader.setFilterPow(i);
    }
    
    /*
    OSC
    */
    public void setUseOSC(boolean b){        
        if(b){              
            startOSCOut(oscPort);            
        }
        else{
            stopOSCOut();
        }
    }
    
    protected void startOSCOut(int port){        
        try {            
            oscOut = new OSCPortOut(InetAddress.getLocalHost(), port);            
            useOSC = true;
            zeroMQReader.setOSCout(oscOut);
            csvReader.setOSCout(oscOut);
        } catch (IOException ex) {
            useOSC = false;
            LOGGER.log(Level.WARNING, null, ex);
        }
        LOGGER.log(Level.INFO, String.format("startOSCOut port %d : %b", port, useOSC));
    }
    
    protected void stopOSCOut(){        
        useOSC = false;
        zeroMQReader.setOSCout(null);
        csvReader.setOSCout(null);
        if(oscOut!=null){
            try {
                oscOut.disconnect();
            } catch (IOException ex) {           
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        LOGGER.log(Level.INFO, String.format("stopOSCOut : %b",  !useOSC));
    }
    
    protected int getOscOutPort(){
        return oscPort;
    }
    
    protected void setOscOutPort(int port){
        LOGGER.log(Level.INFO, String.format("setOscOutPort : %d",  port));
        oscPort = port;      
    }

    private void setConnected(boolean connected) {
        if (connected) {
            actualConnectedProperty = connectedProperty;
            csvConnectedLabel.setForeground(green);
            zeroMQConnectedLabel.setForeground(green);
            csvConnectButton.setText("Disconnect");
            zeroMQConnectButton.setText("Disconnect");
        } else {
            actualConnectedProperty = notConnectedProperty;
            performCheckBox.setSelected(false);
            csvConnectedLabel.setForeground(red);
            zeroMQConnectedLabel.setForeground(red);
            csvConnectButton.setText("Connect");
            zeroMQConnectButton.setText("Connect");
        }
        updateConnectedLabels();
        updateIOPanelsEnabled(connected);
        filterCheckBox.setSelected(zeroMQReader.isUseFilter());
    }

    private void updateCSVReader() {
        if (!csvReader.getFileName().equals(csvFileTextField.getText())) {
            csvReader.setFileName(csvFileTextField.getText());
        }
    }

    private void updateZeroMQReader() {
        if (!zeroMQReader.getHost().equals(zeroMQHostTextField.getText())
                || !zeroMQReader.getPort().equals(zeroMQPortTextField.getText())) {
            zeroMQReader.setURL(zeroMQHostTextField.getText(), zeroMQPortTextField.getText());
        }
    }

    @Override
    public void setLocale(Locale l) {
        super.setLocale(l);
        updateConnectedLabels();
        updateLabelWithColon(csvStatusLabel, statusProperty);
        updateLabelWithColon(csvFileLabel, fileProperty);
        updateLabelWithColon(zeroMQStatusLabel, statusProperty);
        updateLabelWithColon(zeroMQHostLabel, hostProperty);
        updateLabelWithColon(zeroMQPortLabel, portProperty);
    }

    private void updateConnectedLabels() {
        if (csvConnectedLabel != null) {
            csvConnectedLabel.setText(IniManager.getLocaleProperty(actualConnectedProperty));
        }
        if (zeroMQConnectedLabel != null) {
            zeroMQConnectedLabel.setText(IniManager.getLocaleProperty(actualConnectedProperty));
        }
    }

    private void updateLabelWithColon(javax.swing.JLabel label, String property) {
        if (label != null) {
            label.setText(IniManager.getLocaleProperty(property) + ":");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        csvFileChooser = new javax.swing.JFileChooser();
        mainPanel = new javax.swing.JPanel();
        northPanel = new javax.swing.JPanel();
        inputTabbedPane = new javax.swing.JTabbedPane();
        csvTab = new javax.swing.JPanel();
        csvStatusPanel = new javax.swing.JPanel();
        csvStatusLabel = new javax.swing.JLabel();
        csvStatusPanelFiller = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        csvConnectedLabel = new javax.swing.JLabel();
        csvConnectorPanel = new javax.swing.JPanel();
        csvFileLabel = new javax.swing.JLabel();
        csvTabFiller1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        csvFileTextField = new javax.swing.JTextField();
        csvTabFiller2 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        csvOpenButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.open");
        csvTabFiller3 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        csvConnectButton = new javax.swing.JButton();
        zeroMQTab = new javax.swing.JPanel();
        zeroMQStatusPanel = new javax.swing.JPanel();
        zeroMQStatusLabel = new javax.swing.JLabel();
        zeroMQStatusPanelFiller = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        zeroMQConnectedLabel = new javax.swing.JLabel();
        zeroMQConnectorPanel = new javax.swing.JPanel();
        zeroMQHostLabel = new javax.swing.JLabel();
        zeroMQConnectorPanelFiller1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        zeroMQHostTextField = new javax.swing.JTextField();
        zeroMQConnectorPanelFiller2 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        zeroMQPortLabel = new javax.swing.JLabel();
        zeroMQConnectorPanelFiller3 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        zeroMQPortTextField = new javax.swing.JTextField();
        zeroMQConnectorPanelFiller4 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        zeroMQConnectButton = new javax.swing.JButton();
        northPanelFiller1 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        jPanel1 = new javax.swing.JPanel();
        performCheckBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        filterCheckBox = new javax.swing.JCheckBox();
        jSpinnerfilterMaxQueueSize = new javax.swing.JSpinner();
        jSpinnerfilterPow = new javax.swing.JSpinner();
        jCheckBoxSendOSC = new javax.swing.JCheckBox();
        jSpinnerSendOSCPort = new javax.swing.JSpinner();
        northPanelFiller2 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        centerPanel = new javax.swing.JPanel();
        separator = new javax.swing.JSeparator();
        outputPanel = new javax.swing.JPanel();
        outputScrollPane = new javax.swing.JScrollPane();
        featuresTable = new javax.swing.JTable();
        outputButtonPanel = new javax.swing.JPanel();
        setButton = new javax.swing.JButton();
        buttonPanelFiller1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        selectAllButton = new javax.swing.JButton();
        selectNoneButton = new javax.swing.JButton();
        buttonPanelFiller2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();

        csvFileChooser.setFileFilter(csvReader.getFileFilter());

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new java.awt.BorderLayout(0, 10));

        northPanel.setLayout(new javax.swing.BoxLayout(northPanel, javax.swing.BoxLayout.LINE_AXIS));

        inputTabbedPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Stream to read:"));
        inputTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                inputTabbedPaneStateChanged(evt);
            }
        });

        csvTab.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        csvTab.setLayout(new javax.swing.BoxLayout(csvTab, javax.swing.BoxLayout.PAGE_AXIS));

        csvStatusPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 5));

        csvStatusLabel.setText(IniManager.getLocaleProperty("word.status")+":");
        csvStatusPanel.add(csvStatusLabel);
        csvStatusPanel.add(csvStatusPanelFiller);

        csvConnectedLabel.setText(IniManager.getLocaleProperty("network.notconnected"));
        csvStatusPanel.add(csvConnectedLabel);

        csvTab.add(csvStatusPanel);

        csvConnectorPanel.setLayout(new javax.swing.BoxLayout(csvConnectorPanel, javax.swing.BoxLayout.LINE_AXIS));

        csvFileLabel.setText(IniManager.getLocaleProperty("GUI.file")+":");
        csvFileLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        csvConnectorPanel.add(csvFileLabel);
        csvConnectorPanel.add(csvTabFiller1);

        csvFileTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                csvFileTextFieldFocusLost(evt);
            }
        });
        csvFileTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvFileTextFieldActionPerformed(evt);
            }
        });
        csvConnectorPanel.add(csvFileTextField);
        csvConnectorPanel.add(csvTabFiller2);

        csvOpenButton.setName(""); // NOI18N
        csvOpenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvOpenButtonActionPerformed(evt);
            }
        });
        csvConnectorPanel.add(csvOpenButton);
        csvConnectorPanel.add(csvTabFiller3);

        csvConnectButton.setText("Connect");
        csvConnectButton.setPreferredSize(new java.awt.Dimension(93, 23));
        csvConnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvConnectButtonActionPerformed(evt);
            }
        });
        csvConnectorPanel.add(csvConnectButton);

        csvTab.add(csvConnectorPanel);

        inputTabbedPane.addTab("CSV", csvTab);

        zeroMQTab.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        zeroMQTab.setLayout(new javax.swing.BoxLayout(zeroMQTab, javax.swing.BoxLayout.PAGE_AXIS));

        zeroMQStatusPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 5));

        zeroMQStatusLabel.setText(IniManager.getLocaleProperty("word.status")+":");
        zeroMQStatusPanel.add(zeroMQStatusLabel);
        zeroMQStatusPanel.add(zeroMQStatusPanelFiller);

        zeroMQConnectedLabel.setText(IniManager.getLocaleProperty("network.notconnected"));
        zeroMQStatusPanel.add(zeroMQConnectedLabel);

        zeroMQTab.add(zeroMQStatusPanel);

        zeroMQConnectorPanel.setLayout(new javax.swing.BoxLayout(zeroMQConnectorPanel, javax.swing.BoxLayout.LINE_AXIS));

        zeroMQHostLabel.setText(IniManager.getLocaleProperty(hostProperty)+":");
        zeroMQHostLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        zeroMQConnectorPanel.add(zeroMQHostLabel);
        zeroMQConnectorPanel.add(zeroMQConnectorPanelFiller1);

        zeroMQHostTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                zeroMQHostTextFieldFocusLost(evt);
            }
        });
        zeroMQHostTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zeroMQHostTextFieldActionPerformed(evt);
            }
        });
        zeroMQConnectorPanel.add(zeroMQHostTextField);
        zeroMQConnectorPanel.add(zeroMQConnectorPanelFiller2);

        zeroMQPortLabel.setText(IniManager.getLocaleProperty(portProperty)+":");
        zeroMQPortLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        zeroMQConnectorPanel.add(zeroMQPortLabel);
        zeroMQConnectorPanel.add(zeroMQConnectorPanelFiller3);

        zeroMQPortTextField.setMaximumSize(new java.awt.Dimension(50, 2147483647));
        zeroMQPortTextField.setPreferredSize(new java.awt.Dimension(50, 20));
        zeroMQPortTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                zeroMQPortTextFieldFocusLost(evt);
            }
        });
        zeroMQPortTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zeroMQPortTextFieldActionPerformed(evt);
            }
        });
        zeroMQPortTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                zeroMQPortTextFieldKeyTyped(evt);
            }
        });
        zeroMQConnectorPanel.add(zeroMQPortTextField);
        zeroMQConnectorPanel.add(zeroMQConnectorPanelFiller4);

        zeroMQConnectButton.setText("Connect");
        zeroMQConnectButton.setPreferredSize(new java.awt.Dimension(93, 23));
        zeroMQConnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zeroMQConnectButtonActionPerformed(evt);
            }
        });
        zeroMQConnectorPanel.add(zeroMQConnectButton);

        zeroMQTab.add(zeroMQConnectorPanel);

        inputTabbedPane.addTab("ZeroMQ", zeroMQTab);

        northPanel.add(inputTabbedPane);
        northPanel.add(northPanelFiller1);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        performCheckBox.setText("Perform");
        performCheckBox.setMargin(new java.awt.Insets(10, 2, 2, 2));
        performCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performCheckBoxActionPerformed(evt);
            }
        });
        jPanel1.add(performCheckBox);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter"));
        jPanel2.setToolTipText("Fitler");
        jPanel2.setName("Filter"); // NOI18N
        jPanel2.setLayout(new java.awt.GridLayout(2, 2));

        filterCheckBox.setText("On");
        filterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterCheckBoxActionPerformed(evt);
            }
        });
        jPanel2.add(filterCheckBox);

        jSpinnerfilterMaxQueueSize.setModel(new javax.swing.SpinnerNumberModel(5, 1, 100, 1));
        jSpinnerfilterMaxQueueSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerfilterMaxQueueSizeStateChanged(evt);
            }
        });
        jPanel2.add(jSpinnerfilterMaxQueueSize);

        jSpinnerfilterPow.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 1.0d));
        jSpinnerfilterPow.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerfilterPow, "0.00"));
        jSpinnerfilterPow.setValue(zeroMQReader.getFilterPow());
        jSpinnerfilterPow.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerfilterPowStateChanged(evt);
            }
        });
        jPanel2.add(jSpinnerfilterPow);

        jCheckBoxSendOSC.setSelected(useOSC);
        jCheckBoxSendOSC.setText("OSCOut");
        jCheckBoxSendOSC.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxSendOSCStateChanged(evt);
            }
        });
        jPanel2.add(jCheckBoxSendOSC);

        jSpinnerSendOSCPort.setModel(new javax.swing.SpinnerNumberModel(6000, 6000, 99999, 1));
        jSpinnerSendOSCPort.setValue(getOscOutPort());
        jSpinnerSendOSCPort.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerSendOSCPortStateChanged(evt);
            }
        });
        jPanel2.add(jSpinnerSendOSCPort);

        jPanel1.add(jPanel2);

        northPanel.add(jPanel1);
        northPanel.add(northPanelFiller2);

        mainPanel.add(northPanel, java.awt.BorderLayout.NORTH);

        centerPanel.setLayout(new java.awt.BorderLayout(0, 10));
        centerPanel.add(separator, java.awt.BorderLayout.NORTH);

        outputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Available features:"));
        outputPanel.setLayout(new java.awt.BorderLayout(10, 0));

        featuresTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Selected"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        featuresTable.setDragEnabled(true);
        featuresTable.setDropMode(javax.swing.DropMode.ON_OR_INSERT);
        featuresTable.setFillsViewportHeight(true);
        featuresTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        featuresTable.getTableHeader().setReorderingAllowed(false);
        outputScrollPane.setViewportView(featuresTable);

        outputPanel.add(outputScrollPane, java.awt.BorderLayout.CENTER);

        outputButtonPanel.setEnabled(false);
        outputButtonPanel.setLayout(new javax.swing.BoxLayout(outputButtonPanel, javax.swing.BoxLayout.PAGE_AXIS));

        setButton.setText("Set");
        setButton.setMaximumSize(new java.awt.Dimension(89, 23));
        setButton.setMinimumSize(new java.awt.Dimension(89, 23));
        setButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setButtonActionPerformed(evt);
            }
        });
        outputButtonPanel.add(setButton);
        outputButtonPanel.add(buttonPanelFiller1);

        selectAllButton.setText("Select All");
        selectAllButton.setMaximumSize(new java.awt.Dimension(89, 23));
        selectAllButton.setMinimumSize(new java.awt.Dimension(89, 23));
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });
        outputButtonPanel.add(selectAllButton);

        selectNoneButton.setText("Select None");
        selectNoneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNoneButtonActionPerformed(evt);
            }
        });
        outputButtonPanel.add(selectNoneButton);
        outputButtonPanel.add(buttonPanelFiller2);

        upButton.setText("Up");
        upButton.setMaximumSize(new java.awt.Dimension(89, 23));
        upButton.setMinimumSize(new java.awt.Dimension(89, 23));
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });
        outputButtonPanel.add(upButton);

        downButton.setText("Down");
        downButton.setMaximumSize(new java.awt.Dimension(89, 23));
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });
        outputButtonPanel.add(downButton);

        outputPanel.add(outputButtonPanel, java.awt.BorderLayout.EAST);

        centerPanel.add(outputPanel, java.awt.BorderLayout.CENTER);

        mainPanel.add(centerPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateIOPanelsEnabled(boolean enabled) {
        updateInputPanelEnabled(enabled);
        updateOutputPanelEnabled(enabled);
    }

    private void setPanelComponentsEnabled(javax.swing.JPanel panel, boolean enabled) {
        for (java.awt.Component component : panel.getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void updateInputPanelEnabled(boolean enabled) {
        performCheckBox.setEnabled(enabled);
    }

    private void updateOutputPanelEnabled(boolean enabled) {
        outputPanel.setEnabled(enabled);
        setPanelComponentsEnabled(outputButtonPanel, enabled);
    }

    /* ---------------------------------------------------------------------- *
     *                              Tabbed Pane                               *
     * ---------------------------------------------------------------------- */

    private void inputTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_inputTabbedPaneStateChanged
        stopConnections();
    }//GEN-LAST:event_inputTabbedPaneStateChanged

    private void stopConnections() {
        csvReader.stopConnection();
        zeroMQReader.stopConnection();
    }

    /* ---------------------------------------------------------------------- *
     *                                CSV Tab                                 *
     * ---------------------------------------------------------------------- */

    private void csvFileTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvFileTextFieldActionPerformed
        updateCSVReader();
    }//GEN-LAST:event_csvFileTextFieldActionPerformed

    private void csvFileTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_csvFileTextFieldFocusLost
        updateCSVReader();
    }//GEN-LAST:event_csvFileTextFieldFocusLost

    private void csvOpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvOpenButtonActionPerformed
        csvFileChooser.setLocale(Locale.getDefault());
        csvFileChooser.updateUI();
        if (csvFileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            File file = csvFileChooser.getSelectedFile();
            this.csvFileTextField.setText(file.getAbsolutePath());
        }
        updateCSVReader();
    }//GEN-LAST:event_csvOpenButtonActionPerformed

    private void csvConnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvConnectButtonActionPerformed
        if (evt.getActionCommand().equals("Connect")) {
            csvReader.startConnection();
        } else if (evt.getActionCommand().equals("Disconnect")) {
            csvReader.stopConnection();
        }
    }//GEN-LAST:event_csvConnectButtonActionPerformed

    /* ---------------------------------------------------------------------- *
     *                               ZeroMQ Tab                               *
     * ---------------------------------------------------------------------- */

    private void zeroMQHostTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zeroMQHostTextFieldActionPerformed
        updateZeroMQReader();
    }//GEN-LAST:event_zeroMQHostTextFieldActionPerformed

    private void zeroMQHostTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_zeroMQHostTextFieldFocusLost
        updateZeroMQReader();
    }//GEN-LAST:event_zeroMQHostTextFieldFocusLost

    private void zeroMQPortTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_zeroMQPortTextFieldKeyTyped
        char pressed = evt.getKeyChar();
        if (Character.isDigit(pressed)) {
            String beforeselect = zeroMQPortTextField.getText().substring(0, zeroMQPortTextField.getSelectionStart());
            String afterSelect = zeroMQPortTextField.getText().substring(zeroMQPortTextField.getSelectionEnd());
            long value = Long.parseLong(beforeselect + pressed + afterSelect);
            if (0 <= value && value <= 65535) {
                return;//it's ok
            }
        }
        evt.consume();
    }//GEN-LAST:event_zeroMQPortTextFieldKeyTyped

    private void zeroMQPortTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zeroMQPortTextFieldActionPerformed
        updateZeroMQReader();
    }//GEN-LAST:event_zeroMQPortTextFieldActionPerformed

    private void zeroMQPortTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_zeroMQPortTextFieldFocusLost
        updateZeroMQReader();
    }//GEN-LAST:event_zeroMQPortTextFieldFocusLost

    private void zeroMQConnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zeroMQConnectButtonActionPerformed
        if (evt.getActionCommand().equals("Connect")) {
            zeroMQReader.startConnection();
        } else if (evt.getActionCommand().equals("Disconnect")) {
            zeroMQReader.stopConnection();
        }
    }//GEN-LAST:event_zeroMQConnectButtonActionPerformed

    /* ---------------------------------------------------------------------- *
     *                          Output Buttons Panel                          *
     * ---------------------------------------------------------------------- */

    private void setButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButtonActionPerformed
        zeroMQReader.setSelected(getSelectedFeatures());
    }//GEN-LAST:event_setButtonActionPerformed

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) featuresTable.getModel();
        for (int i = 0; i < model.getRowCount(); ++i) {
            model.setValueAt(true, i, 1);
        }
    }//GEN-LAST:event_selectAllButtonActionPerformed

    private void selectNoneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectNoneButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) featuresTable.getModel();
        for (int i = 0; i < model.getRowCount(); ++i) {
            model.setValueAt(false, i, 1);
        }
    }//GEN-LAST:event_selectNoneButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        int selectedIndex = featuresTable.getSelectedRow();
        if (selectedIndex > 0) {
            moveSelectedFeature(selectedIndex, selectedIndex - 1);
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        int selectedIndex = featuresTable.getSelectedRow();
        if (selectedIndex > 0) {
            moveSelectedFeature(selectedIndex, selectedIndex + 1);
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void filterCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterCheckBoxActionPerformed
        zeroMQReader.setUseFilter(filterCheckBox.isSelected());
        csvReader.setUseFilter(filterCheckBox.isSelected());
    }//GEN-LAST:event_filterCheckBoxActionPerformed

    private void performCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_performCheckBoxActionPerformed

    private void jSpinnerfilterPowStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerfilterPowStateChanged
        try {
            jSpinnerfilterPow.commitEdit();
        } catch ( java.text.ParseException e ) {  }
        double value = (Double)jSpinnerfilterPow.getValue();
        setFilterPow(value);
    }//GEN-LAST:event_jSpinnerfilterPowStateChanged

    private void jSpinnerfilterMaxQueueSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerfilterMaxQueueSizeStateChanged
        try {
            jSpinnerfilterMaxQueueSize.commitEdit();
        } catch ( java.text.ParseException e ) {  }
        int value = (Integer)jSpinnerfilterMaxQueueSize.getValue();
        setFilterMaxQueueSize(value);
    }//GEN-LAST:event_jSpinnerfilterMaxQueueSizeStateChanged

    private void jCheckBoxSendOSCStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxSendOSCStateChanged
        // TODO add your handling code here:
        setUseOSC(jCheckBoxSendOSC.isSelected());
    }//GEN-LAST:event_jCheckBoxSendOSCStateChanged

    private void jSpinnerSendOSCPortStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerSendOSCPortStateChanged
        try {
            jSpinnerSendOSCPort.commitEdit();
        } catch ( java.text.ParseException e ) {  }
        int value = (Integer)jSpinnerSendOSCPort.getValue();
        setOscOutPort(value);
    }//GEN-LAST:event_jSpinnerSendOSCPortStateChanged

    /* ---------------------------------------------------------------------- */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler buttonPanelFiller1;
    private javax.swing.Box.Filler buttonPanelFiller2;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JButton csvConnectButton;
    private javax.swing.JLabel csvConnectedLabel;
    private javax.swing.JPanel csvConnectorPanel;
    private javax.swing.JFileChooser csvFileChooser;
    private javax.swing.JLabel csvFileLabel;
    private javax.swing.JTextField csvFileTextField;
    private javax.swing.JButton csvOpenButton;
    private javax.swing.JLabel csvStatusLabel;
    private javax.swing.JPanel csvStatusPanel;
    private javax.swing.Box.Filler csvStatusPanelFiller;
    private javax.swing.JPanel csvTab;
    private javax.swing.Box.Filler csvTabFiller1;
    private javax.swing.Box.Filler csvTabFiller2;
    private javax.swing.Box.Filler csvTabFiller3;
    private javax.swing.JButton downButton;
    private javax.swing.JTable featuresTable;
    private javax.swing.JCheckBox filterCheckBox;
    private javax.swing.JTabbedPane inputTabbedPane;
    private javax.swing.JCheckBox jCheckBoxSendOSC;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSpinner jSpinnerSendOSCPort;
    private javax.swing.JSpinner jSpinnerfilterMaxQueueSize;
    private javax.swing.JSpinner jSpinnerfilterPow;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel northPanel;
    private javax.swing.Box.Filler northPanelFiller1;
    private javax.swing.Box.Filler northPanelFiller2;
    private javax.swing.JPanel outputButtonPanel;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JCheckBox performCheckBox;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JButton selectNoneButton;
    private javax.swing.JSeparator separator;
    private javax.swing.JButton setButton;
    private javax.swing.JButton upButton;
    private javax.swing.JButton zeroMQConnectButton;
    private javax.swing.JLabel zeroMQConnectedLabel;
    private javax.swing.JPanel zeroMQConnectorPanel;
    private javax.swing.Box.Filler zeroMQConnectorPanelFiller1;
    private javax.swing.Box.Filler zeroMQConnectorPanelFiller2;
    private javax.swing.Box.Filler zeroMQConnectorPanelFiller3;
    private javax.swing.Box.Filler zeroMQConnectorPanelFiller4;
    private javax.swing.JLabel zeroMQHostLabel;
    private javax.swing.JTextField zeroMQHostTextField;
    private javax.swing.JLabel zeroMQPortLabel;
    private javax.swing.JTextField zeroMQPortTextField;
    private javax.swing.JLabel zeroMQStatusLabel;
    private javax.swing.JPanel zeroMQStatusPanel;
    private javax.swing.Box.Filler zeroMQStatusPanelFiller;
    private javax.swing.JPanel zeroMQTab;
    // End of variables declaration//GEN-END:variables

    /* ---------------------------------------------------------------------- */

    /**
     * @param index the tab index to select
     */
    public void setTabIndex(int index) {
        inputTabbedPane.setSelectedIndex(index);
    }

    /**
     * @return the selected tab index
     */
    public int getTabIndex() {
        return inputTabbedPane.getSelectedIndex();
    }

    /**
     * @param name the CSV file name to set
     */
    public void setCSVFileName(String name) {
        csvFileTextField.setText(name);
    }

    /**
     * @return the CSV file name
     */
    public String getCSVFileName() {
        return csvFileTextField.getText();
    }

    /**
     * @param host the ZeroMQ host to set
     */
    public void setZeroMQHost(String host) {
        zeroMQHostTextField.setText(host);
    }

    /**
     * @return the ZeroMQ host
     */
    public String getZeroMQHost() {
        return zeroMQHostTextField.getText();
    }

    /**
     * @param port the ZeroMQ port to set
     */
    public void setZeroMQPort(String port) {
        zeroMQPortTextField.setText(port);
    }

    /**
     * @return the ZeroMQ port
     */
    public String getZeroMQPort() {
        return zeroMQPortTextField.getText();
    }

    /**
     * @return the state of the "Perform" button
     */
    public boolean isPerforming() {
        return performCheckBox.isSelected();
    }

    /* ---------------------------------------------------------------------- */

    private void updateFeatures(String[] newFeatures) {
        DefaultTableModel model = (DefaultTableModel) featuresTable.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; --i) {
            model.removeRow(i);
        }
        for (String feature : newFeatures) {
            model.addRow(new Object[]{feature, false});
        }
    }

    private String[] getSelectedFeatures() {
        DefaultTableModel model = (DefaultTableModel) featuresTable.getModel();
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); ++i) {
            if ((Boolean) model.getValueAt(i, 1)) {
                selected.add((String) model.getValueAt(i, 0));
            }
        }
        return selected.toArray(new String[selected.size()]);
    }

    private void moveSelectedFeature(int old, int newIndex) {
        DefaultTableModel model = (DefaultTableModel) featuresTable.getModel();
        model.moveRow(old, old, newIndex);
        featuresTable.setRowSelectionInterval(old, newIndex);
    }

    /* ---------------------------------------------------------------------- *
     *                               AUEmitter                                *
     * ---------------------------------------------------------------------- */

    @Override
    public void addAUPerformer(AUPerformer auPerformer) {
        if (auPerformer != null) {
            LOGGER.fine("addAUPerformer");
            auEmitter.addAUPerformer(auPerformer);
        }
    }

    @Override
    public void removeAUPerformer(AUPerformer auPerformer) {
        if (auPerformer != null) {
            LOGGER.fine("removeAUPerformer");
            auEmitter.removeAUPerformer(auPerformer);
        }
    }

    public void sendAUFrame(AUAPFrame auFrame, ID id) {
        LOGGER.info("sendAUFrame");
        auEmitter.performAUAPFrame(auFrame, id);
    }

    /* ---------------------------------------------------------------------- *
     *                            BAPFrameEmitter                             *
     * ---------------------------------------------------------------------- */

    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapFramePerformer) {
        if (bapFramePerformer != null) {
            LOGGER.fine("addBAPFramePerformer");
            bapFrameEmitter.addBAPFramePerformer(bapFramePerformer);
        }
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapFramePerformer) {
        if (bapFramePerformer != null) {
            LOGGER.fine("removeBAPFramePerformer");
            bapFrameEmitter.removeBAPFramePerformer(bapFramePerformer);
        }
    }

    public void sendBAPFrame(BAPFrame bapFrame, ID id) {
        //LOGGER.info("sendBAPFrame");
        bapFrameEmitter.sendBAPFrame(id, bapFrame);
    }

    /* ---------------------------------------------------------------------- *
     *                           ConnectionListener                           *
     * ---------------------------------------------------------------------- */

    @Override
    public void onConnection() {
        setConnected(true);
    }

    @Override
    public void onDisconnection() {
        setConnected(false);
    }

    /* ---------------------------------------------------------------------- *
     *                          StringArrayListener                           *
     * ---------------------------------------------------------------------- */

    @Override
    public void stringArrayChanged(String[] newFeatures) {
        updateFeatures(newFeatures);
    }
}
