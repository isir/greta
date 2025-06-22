/*
 * This file is part of Greta.
 * Licensed under the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package greta.auxiliary.textinput;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Text Input module for Greta ECA System
 * Allows users to type text that will be sent to avatar for speech and animation
 */
public class TextInputFrame extends JFrame {
    
    private JTextArea textArea;
    private JButton sendButton;
    private JButton clearButton;
    private JLabel statusLabel;
    private TextSender sender;
    private int messageCounter = 1;
    
    public TextInputFrame() {
        super("Text Input - Greta ECA");
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        // Text input area
        textArea = new JTextArea(8, 40);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Buttons
        sendButton = new JButton("🗣️ Send to Avatar");
        sendButton.setPreferredSize(new Dimension(150, 35));
        sendButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        clearButton = new JButton("🗑️ Clear");
        clearButton.setPreferredSize(new Dimension(100, 35));
        
        // Status
        statusLabel = new JLabel("💡 Type text and press Send to make avatar speak");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);
        
        updateButtonStates();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Main text area with scroll
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(new TitledBorder("Enter text for avatar to speak:"));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(clearButton);
        buttonPanel.add(sendButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.NORTH);
        
        // Add some padding
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void setupEventHandlers() {
        // Send button action
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendText();
            }
        });
        
        // Clear button action
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearText();
            }
        });
        
        // Ctrl+Enter to send
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    sendText();
                    e.consume();
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                updateButtonStates();
            }
        });
        
        // Update button states when text changes
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateButtonStates(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateButtonStates(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateButtonStates(); }
        });
    }
    
    private void updateButtonStates() {
        boolean hasText = !textArea.getText().trim().isEmpty();
        sendButton.setEnabled(hasText && sender != null);
        clearButton.setEnabled(hasText);
        
        if (sender == null) {
            statusLabel.setText("⚠️ Not connected to avatar system");
            statusLabel.setForeground(Color.RED);
        } else if (hasText) {
            statusLabel.setText("✅ Ready to send - Press Send or Ctrl+Enter");
            statusLabel.setForeground(Color.GREEN.darker());
        } else {
            statusLabel.setText("💡 Type text and press Send to make avatar speak");
            statusLabel.setForeground(Color.GRAY);
        }
    }
    
    private void sendText() {
        String text = textArea.getText().trim();
        if (text.isEmpty() || sender == null) {
            return;
        }
        
        try {
            // Create metadata map
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("content-id", "textInput_" + System.currentTimeMillis());
            metadata.put("message-number", messageCounter++);
            metadata.put("source", "TextInputFrame");
            metadata.put("timestamp", System.currentTimeMillis());
            
            // Send to avatar system
            sender.send(text, metadata);
            
            // Update UI
            statusLabel.setText("📤 Sent: \"" + truncateText(text, 30) + "\"");
            statusLabel.setForeground(Color.BLUE);
            
            // Auto-clear after successful send
            Timer timer = new Timer(2000, e -> {
                clearText();
                updateButtonStates();
            });
            timer.setRepeats(false);
            timer.start();
            
            System.out.println("Text sent to avatar: " + text);
            
        } catch (Exception e) {
            statusLabel.setText("❌ Error sending text: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            System.err.println("Error sending text: " + e.getMessage());
        }
    }
    
    private void clearText() {
        textArea.setText("");
        textArea.requestFocus();
        updateButtonStates();
    }
    
    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
    
    // Greta module integration methods
    public void setSender(TextSender sender) {
        this.sender = sender;
        updateButtonStates();
        System.out.println("TextInputFrame: Sender connected - " + (sender != null ? "SUCCESS" : "NULL"));
    }
    
    public TextSender getSender() {
        return sender;
    }
    
    public void setText(String text) {
        SwingUtilities.invokeLater(() -> {
            textArea.setText(text);
            updateButtonStates();
        });
    }
    
    public String getText() {
        return textArea.getText();
    }
}