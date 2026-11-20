/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.modiff;

/**
 *
 * @author Michele
 */
import java.io.*;  
import static java.lang.Thread.MAX_PRIORITY;
import java.net.*;  
import java.util.logging.Level;
import java.util.logging.Logger;
public class Server {  

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
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
    private PrintWriter out;
    private BufferedReader in;
    public boolean stop=false;
    public boolean connected=false;

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
        port="4000";
        address="10.51.18.246";
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
    
    
  
    public void startConnection() throws IOException {
        
        
        InetAddress addr= InetAddress.getByName(address);
        System.out.println("Trying to open port " + port + "... at address:"+address);
        serverSocket = new ServerSocket(Integer.valueOf(port),MAX_PRIORITY,addr);
        System.out.println("Trying to open port " + port + "... at address:"+address);
        clientSocket = serverSocket.accept();
        System.out.println("Instantiating input and output streams...");
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("greta.auxiliary.asap.Server.startConnection()");
        System.out.println("greta.auxiliary.asap.Server.startConnection()");
    }

    public void sendMessage(String msg) throws IOException {
        out.println(msg);
    }
    
    /*public String receiveMessage() throws IOException{
        
        System.out.println("NOUS ESSAYONS DE LIRE LE MESSAGE");
        
        String resp = in.readLine();
        System.out.println("Le message est "+ resp);
        
        return resp;
    }*/
    public String receiveMessage() throws IOException {
    //System.out.println("Waiting to receive message...");
    String resp = in.readLine();
    if (resp == null) {
        System.out.println("End of stream reached or connection closed.");
        throw new IOException("End of stream reached or connection closed.");
    }
    //System.out.println("Received message: " + resp);
    return resp;
}

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
    
        
    }

  