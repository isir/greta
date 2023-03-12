/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.input;

/**
 *
 * @author NEZIH YOUNSI
 */

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client {
    private Socket clientSocket;
    private DataInputStream in;
    private PrintWriter out;
    public String address;
    public String port;
    
    public boolean stop = false;
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

    public Client(){
        address="localhost";
        port="50150";
    }
    
    public Client(String a, String p){
        address=a;
        port=p;
    }  

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
    
    // Methode qui crée le client, ensuite appelé dans l'input manager qui crée le client via un thread des l'initialisation de flipper. (methode init)
    public void startConnection() throws IOException {
        //lock.lock();
        //try {
            if (!connected) {
                connected = true;

                System.out.println("Trying to connect to server at " + address + ":" + port + "...");
                clientSocket = new Socket(address, Integer.valueOf(port));
                System.out.println("Connected to server");
                System.out.println("Instantiating input and output streams...");
                in = new DataInputStream(clientSocket.getInputStream());
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            }
        //} finally {
           // lock.unlock();
        //}
    }

    //methode Qui recoit les données openface envoyés.
    public double receivePositivity() throws IOException {
        double[] aus = new double[3];
        aus[0] = in.readDouble();
        aus[1] = in.readDouble();
        aus[2] = in.readDouble();
        System.out.println("Received au1: " + aus[0]);
        System.out.println("Received au12: " + aus[1]);
        System.out.println("Received au2: " + aus[2]);
        
        double positivity = (aus[0] + aus[2] + aus[1]) * 1/3;
        // double positivity = ComputePositivity(aus[0],aus[1],aus[2], w1,w2,w3,threshold);
        System.out.println("Received POSITIVITY :" + String.valueOf(positivity));
        return positivity;
    }

    //AJOUTER UNE METHODE COMPUTE POSITIVITY QUI SERA APPLé DANS receivePositivity POUR COMPUTE UNE POSITIVT2 EN SE BASANT SUR LES AUS ET HEADNOD RECU
    
   /* public double ComputePositivity(double au_1, double au_2, double au_3, double w1, double w2, double w3, double threshold){
        
        positivity = au_1*w1 + au_2*w2 ... etc
        if (positivity > threshold){
            return positivity
        }
        else {
            return positivity = 0;
        }
    }*/
    
    public void sendMessage(String message) {
        out.println(message);
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}

