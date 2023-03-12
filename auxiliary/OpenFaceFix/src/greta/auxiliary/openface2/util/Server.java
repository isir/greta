/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author NEZIH YOUNSI
 */

package greta.auxiliary.openface2.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Thread.MAX_PRIORITY;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;  
import static java.lang.Thread.MAX_PRIORITY;
import java.net.*;  
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {  

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public String port;
    public String address;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream out;
    private BufferedReader in;
    public boolean stop=false;
    public Lock lock = new ReentrantLock();
    public boolean connected = false;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
    

    public Server(){
        port="50130";
        address="localhost";
    }
    
    public Server(String p, String a){
        port=p;
        address=a;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    
  //La mathode qui initialise les paramètres du serveur qui envoit les données openface vers flipper. 
    public void startConnection() throws IOException {

        
                InetAddress addr= InetAddress.getByName(address);
                System.out.println("Trying to open port " + port + "... at address:"+address);
                serverSocket = new ServerSocket(Integer.valueOf(port),MAX_PRIORITY,addr);
                System.out.println("Trying to open port " + port + "... at address:"+address);
                System.out.println("Waiting for client to connect...");
                clientSocket = serverSocket.accept();
                System.out.println("Client is connected to server");
                System.out.println("Instantiating input and output streams...");
                out = new DataOutputStream(clientSocket.getOutputStream());
                in =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                System.out.println("greta.openface.util.Server.startConnection()");
                System.out.println("greta.openface.util.Server.startConnection()");
                this.connected = true;

    }     

    //La methode qui envoient les paramètre par socketing via le server. (cette méthode est applé dans la methode sendPositivityAus dans la loop d'envoi sur Openface Frame)
    public void sendPositivity( double au1, double au12, double au2) throws IOException {

                out.writeDouble(au1);
                out.writeDouble(au12);
                out.writeDouble(au2);
                out.flush();
                System.out.println("Data sent: au1 = " + au1 + ", au12 = " + au12+ ", au2 = " + au2);
            
    }
    
    public String receiveMessage() throws IOException{
        
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
    
        
    }

  