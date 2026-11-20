/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Michele
 */
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
 
/**
 * This program demonstrates a simple TCP/IP socket server that echoes every
 * message from the client in reversed form.
 * This server is multi-threaded.
 *
 * @author www.codejava.net
 */
import static java.lang.Thread.MAX_PRIORITY;
public class Test {
 
    public static void main(String[] args) throws UnknownHostException {
 
        int port = 100;
        InetAddress in = InetAddress.getByName("10.51.18.246");
        System.out.println(in.getHostName());
        try (ServerSocket serverSocket = new ServerSocket(port,MAX_PRIORITY,in)){
            System.out.println("Server is listening on port " + port);
            System.out.println(serverSocket.isBound()+" "+serverSocket.getLocalSocketAddress());
 
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

            }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }  
}