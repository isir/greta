/*
 * This file is part of Greta.
 * Licensed under the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package greta.auxiliary.player.webavatar;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebGL-based 3D Avatar Player for Greta ECA System
 * Provides avatar rendering through web browser with WebSocket communication
 */
public class WebAvatarPlayer extends JFrame {
    
    private WebSocketServer webSocketServer;
    private List<WebSocket> connectedClients;
    private ObjectMapper jsonMapper;
    private JLabel statusLabel;
    private JButton openBrowserButton;
    private boolean isServerRunning = false;
    
    // Animation state
    private String currentCharacter = "alice";
    private Map<String, Object> currentAnimationState;
    
    public WebAvatarPlayer() {
        super("WebGL Avatar Player");
        connectedClients = new ArrayList<>();
        jsonMapper = new ObjectMapper();
        currentAnimationState = new HashMap<>();
        
        initializeGUI();
        startWebSocketServer();
    }
    
    private void initializeGUI() {
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(400, 300);
        setLayout(new BorderLayout());
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout());
        statusLabel = new JLabel("🔴 Avatar Server: Starting...");
        statusPanel.add(statusLabel);
        
        // Control panel
        JPanel controlPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        openBrowserButton = new JButton("🌐 Open Avatar in Browser");
        openBrowserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAvatarInBrowser();
            }
        });
        
        JButton testAnimationButton = new JButton("🎭 Test Avatar Animation");
        testAnimationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testAnimation();
            }
        });
        
        JButton changeCharacterButton = new JButton("👤 Change Character");
        changeCharacterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeCharacter();
            }
        });
        
        controlPanel.add(openBrowserButton);
        controlPanel.add(testAnimationButton);
        controlPanel.add(changeCharacterButton);
        
        // Info panel
        JTextArea infoArea = new JTextArea(
            "WebGL 3D Avatar Player\\n\\n" +
            "Features:\\n" +
            "• Real-time 3D avatar rendering\\n" +
            "• Facial expressions and lip sync\\n" +
            "• Gesture animation\\n" +
            "• Multiple character models\\n\\n" +
            "Click 'Open Avatar in Browser' to view your 3D avatar!"
        );
        infoArea.setEditable(false);
        infoArea.setBackground(getBackground());
        JScrollPane scrollPane = new JScrollPane(infoArea);
        
        add(statusPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
    }
    
    private void startWebSocketServer() {
        try {
            webSocketServer = new WebSocketServer(new InetSocketAddress(8081)) {
                @Override
                public void onOpen(WebSocket conn, ClientHandshake handshake) {
                    connectedClients.add(conn);
                    System.out.println("Avatar client connected: " + conn.getRemoteSocketAddress());
                    updateStatus();
                    
                    // Send initial character configuration
                    sendToClient(conn, "character", Map.of("name", currentCharacter));
                }
                
                @Override
                public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                    connectedClients.remove(conn);
                    System.out.println("Avatar client disconnected: " + conn.getRemoteSocketAddress());
                    updateStatus();
                }
                
                @Override
                public void onMessage(WebSocket conn, String message) {
                    System.out.println("Message from avatar client: " + message);
                }
                
                @Override
                public void onError(WebSocket conn, Exception ex) {
                    System.err.println("WebSocket error: " + ex.getMessage());
                    ex.printStackTrace();
                }
                
                @Override
                public void onStart() {
                    System.out.println("Avatar WebSocket server started on port 8081");
                    isServerRunning = true;
                    SwingUtilities.invokeLater(() -> updateStatus());
                }
            };
            
            webSocketServer.start();
            
        } catch (Exception e) {
            System.err.println("Failed to start WebSocket server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateStatus() {
        SwingUtilities.invokeLater(() -> {
            if (isServerRunning) {
                statusLabel.setText(String.format("🟢 Avatar Server: Running (%d clients)", connectedClients.size()));
                openBrowserButton.setEnabled(true);
            } else {
                statusLabel.setText("🔴 Avatar Server: Stopped");
                openBrowserButton.setEnabled(false);
            }
        });
    }
    
    private void openAvatarInBrowser() {
        try {
            String url = "http://localhost:8080/avatar.html";
            Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            System.err.println("Failed to open browser: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Could not open browser automatically.\\n" +
                "Please open: http://localhost:8080/avatar.html", 
                "Avatar Viewer", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void testAnimation() {
        if (connectedClients.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No avatar clients connected.\\nPlease open the avatar in browser first.", 
                "No Clients", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Test facial expression
        Map<String, Object> expression = Map.of(
            "type", "expression",
            "name", "smile",
            "intensity", 0.8,
            "duration", 2000
        );
        
        broadcastToClients("animation", expression);
    }
    
    private void changeCharacter() {
        String[] characters = {"alice", "camille", "emma", "franck"};
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "Select Avatar Character:",
            "Change Character",
            JOptionPane.QUESTION_MESSAGE,
            null,
            characters,
            currentCharacter
        );
        
        if (selected != null && !selected.equals(currentCharacter)) {
            currentCharacter = selected;
            broadcastToClients("character", Map.of("name", currentCharacter));
        }
    }
    
    private void broadcastToClients(String type, Object data) {
        Map<String, Object> message = Map.of("type", type, "data", data);
        sendToAllClients(message);
    }
    
    private void sendToClient(WebSocket client, String type, Object data) {
        try {
            Map<String, Object> message = Map.of("type", type, "data", data);
            String json = jsonMapper.writeValueAsString(message);
            client.send(json);
        } catch (IOException e) {
            System.err.println("Failed to send message to client: " + e.getMessage());
        }
    }
    
    private void sendToAllClients(Object message) {
        try {
            String json = jsonMapper.writeValueAsString(message);
            for (WebSocket client : new ArrayList<>(connectedClients)) {
                try {
                    client.send(json);
                } catch (Exception e) {
                    System.err.println("Failed to send to client: " + e.getMessage());
                    connectedClients.remove(client);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to serialize message: " + e.getMessage());
        }
    }
    
    // Getters and setters for Modular system integration
    public String getCurrentCharacter() {
        return currentCharacter;
    }
    
    public void setCurrentCharacter(String character) {
        this.currentCharacter = character;
        broadcastToClients("character", Map.of("name", character));
    }
    
    public int getWebSocketPort() {
        return 8081;
    }
    
    public void setWebSocketPort(int port) {
        // Port setting would require server restart
        System.out.println("WebSocket port change requires restart: " + port);
    }
}