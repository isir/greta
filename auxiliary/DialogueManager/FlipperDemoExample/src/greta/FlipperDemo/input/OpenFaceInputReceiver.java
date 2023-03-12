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
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class OpenFaceInputReceiver {

    private static final String SERVER_IP = "127.0.0.1";
    private static int port = 5001;
    private double positivity =0.0;


    public OpenFaceInputReceiver(int port) {
        this.port = port;
    }

    public void StartConnexion() {
        new Thread(() -> {
            try {
                Socket clientSocket = new Socket(SERVER_IP, port);
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                while (true) {
                    double au6 = in.readDouble();
                    double au12 = in.readDouble();
                    System.out.println("Received AU06: " + au6 + "; AU12: " + au12);
                    positivity = (au6 + au12 )/2;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    public boolean hasSmile(){
        // if chunk or np frame = chunk size
        //return true 
        return positivity != 0.0;
    }
     
    public double getPositivity(){
        return this.positivity;
    }
    
    public int getPort(){
        return this.port;
    }

}