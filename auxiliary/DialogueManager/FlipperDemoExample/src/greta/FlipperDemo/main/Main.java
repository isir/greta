package greta.FlipperDemo.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import hmi.flipper2.launcher.FlipperLauncherThread;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class.getName());
    private static FlipperLauncherThread flt;
  
    public Main(){
        init();
    }
   public void init(){
        String help = "Expecting commandline arguments in the form of \"-<argname> <arg>\".\nAccepting the following argnames: config";
        //String flipperPropFile = "flipper/flipperDemo.properties";
        String flipperPropertyRes = "Common/Data/FlipperResources/flipperDemo.properties";
       //String flipperPropertyRes = "C:/NewWork/greta_master/auxiliary/DialogueManager/Flipper-2.0-example-master/Flipper-2.0-example-master/src/main/resources/flipper/flipper.properties";

        

        Properties ps = new Properties();
         InputStream inputstream = null;
 
        try {
            inputstream = new FileInputStream(flipperPropertyRes);
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        
       // InputStream flipperPropStream = Main.class.getClassLoader().getResourceAsStream(flipperPropFile);
        try {
            ps.load(inputstream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("Flipperlauncher: Starting Thread");
        flt = new FlipperLauncherThread(ps);
        flt.start();


    }
    public static void main(String[] args){
        String help = "Expecting commandline arguments in the form of \"-<argname> <arg>\".\nAccepting the following argnames: config";
        String flipperPropFile = "flipper/flipper.properties";
        String flipperPropertyRes = "C:/NewWork/greta_master/bin/Common/Data/FlipperResources/flipperDemo.properties";

        if (args.length % 2 != 0) {
            System.err.println(help);
            System.exit(0);
        }

        for (int i = 0; i < args.length; i = i + 2) {
            if (args[i].equals("-config")) {
                flipperPropertyRes = args[i + 1];
            } else {
                System.err.println("Unknown commandline argument: \"" + args[i] + " " + args[i + 1] + "\".\n" + help);
                System.exit(0);
            }
        }

        Properties ps = new Properties();
         InputStream inputstream = null;
 
        try {
            inputstream = new FileInputStream(flipperPropertyRes);
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        
       // InputStream flipperPropStream = Main.class.getClassLoader().getResourceAsStream(flipperPropFile);
        try {
            ps.load(inputstream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("Flipperlauncher: Starting Thread");
        flt = new FlipperLauncherThread(ps);
        flt.start();


    }
}
