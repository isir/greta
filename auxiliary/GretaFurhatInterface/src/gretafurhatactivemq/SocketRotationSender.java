/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gretafurhatactivemq;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Admin
 */
    
public class SocketRotationSender {
    private final int port;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader inStream;
    private PrintWriter outStream;

    public SocketRotationSender(int port) {
        this.port = port;
    }

    public void start() {
        try{
        serverSocket = new ServerSocket(port);
        System.out.println("Socket server started on port " + port);

        // Accept client connection
        clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

        // Initialize input and output streams
        inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outStream = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        outStream.println(message);
        System.out.println("Sent message: " + message);
    }

    public String receiveMessage() {
        try{
        String receivedMessage = inStream.readLine();
        System.out.println("Received message from client: " + receivedMessage);
        return receivedMessage;
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public void stop() {
        try{
        // Close resources
        inStream.close();
        outStream.close();
        clientSocket.close();
        serverSocket.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}