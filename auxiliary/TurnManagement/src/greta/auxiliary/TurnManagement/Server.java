/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.TurnManagement;

/**
 *
 * @author Michele
 */
import java.io.*;  
import static java.lang.Thread.MAX_PRIORITY;
import java.net.*;  
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.charset.StandardCharsets;
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
    
    private String encoding;

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
        port="5960";
        //address="10.51.18.246";
        address = "localhost";
        encoding = "UTF-8";
//        encoding = "ISO-8859-1";
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
        serverSocket = new ServerSocket(Integer.valueOf(port),MAX_PRIORITY);
        System.out.println("Instantiating input and output streams...");
        System.out.println("greta.auxiliary.TurnManagement.Server.startConnection()");
    }
    
    public void accept_new_connection() throws IOException{
        clientSocket = serverSocket.accept();
        in =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), encoding));
        out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), encoding), true);
        System.out.println("greta.auxiliary.TurnManagement.Server.accept_new_connection(): connection established");
    }

    public void accept_new_connection(String server_name) throws IOException{
        clientSocket = serverSocket.accept();
        in =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), encoding), 1024);
        out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), encoding), true);
        System.out.format("greta.auxiliary.TurnManagement.Server.accept_new_connection(): %s connection established%n", server_name);
    }    
    
    public void sendMessage(String msg) throws IOException {
        out.println(msg);
    }
    
    public String receiveMessage() throws IOException, InterruptedException{
//        while (!in.ready()) {
//            System.out.println("greta.auxiliary.TurnManagement.Server.receiveMessage(): waiting to receive message");
//            Thread.sleep(100);
//        }
//        if (in.ready())
//        {
//            String resp = in.readLine();
//            System.out.println(resp);
//            return resp;
//        }
//        else {
//            return "";
//        }
        
        //        }
        try {
            String resp = in.readLine();
//            System.out.println(resp);
            return resp;
        }
        catch (IOException ex) {
            return "";
        }
        
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
    
        
    }

  