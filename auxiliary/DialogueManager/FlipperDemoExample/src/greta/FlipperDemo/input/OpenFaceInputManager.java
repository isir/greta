/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.input;

import greta.FlipperDemo.main.FlipperLauncherMain;

/**
 *
 * @author NEZIH YOUNSI
 */
public class OpenFaceInputManager {
    
    private OpenFaceInputReceiver receiver;
    private FlipperLauncherMain singletoneInstance = null;
    private String host = null;
    private int port = receiver.getPort();

    /*public void Connect (int port){
        receiver = new OpenFaceInputReceiver(port);
        receiver.StartConnexion();
    }*/
    
    public boolean init()
   {   System.out.println("Openface input manager initialized");
       singletoneInstance = FlipperLauncherMain.getInstance();
       if(singletoneInstance != null){
           System.out.println("jai gayatri mata: openface input got main singleton instance : openface");
       }
       receiver = new OpenFaceInputReceiver(port);
       receiver.StartConnexion();
       //Connect(port);
       
       return true;
   }
    
    public String getPositivity(){
        double pos= receiver.getPositivity();
        return String.valueOf(pos);
    }
    
    public boolean hasSmile(){
        return receiver.hasSmile();
    }

}


