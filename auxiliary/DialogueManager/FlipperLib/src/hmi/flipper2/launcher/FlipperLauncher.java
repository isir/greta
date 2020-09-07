package hmi.flipper2.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import hmi.flipper2.FlipperException;
import hmi.flipper2.TemplateController;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;

// TODO: Add database support
public class FlipperLauncher {

	private static Logger logger = LoggerFactory.getLogger(FlipperLauncher.class.getName());
	private static FlipperLauncherThread flt;
        public FlipperLauncher(){
            init();
        }
	
//	public static void main(String[] args) {
        public void init(){
		String help = "Expecting commandline arguments in the form of \"-<argname> <arg>\".\nAccepting the following argnames: config";
    	String flipperPropFile = "flipper.properties";
      //  String flipperPropertyRes = "Common/Data/FlipperResources/flipper.properties";
        String flipperPropertyRes = "Common/Data/FlipperResources/flipper.properties";
    	
     /*   if(args.length % 2 != 0){0
        
        
        
        	System.err.println(help);
        	System.exit(0);
        }
        
        for(int i = 0; i < args.length; i = i + 2){
        	if(args[i].equals("-config")) {
        		flipperPropFile = args[i+1];
        	} else {
            	System.err.println("Unknown commandline argument: \""+args[i]+" "+args[i+1]+"\".\n"+help);
            	System.exit(0);
        	}
        }*/
        
        InputStream inputstream = null;
            try {
                inputstream = new FileInputStream(flipperPropertyRes);
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(FlipperLauncher.class.getName()).log(Level.SEVERE, null, ex);
            }
        Properties ps = new Properties();
      //  InputStream flipperPropStream = FlipperLauncher.class.getClassLoader().getResourceAsStream(flipperPropFile);

        try {
           // ps.load(flipperPropStream);
              ps.load(inputstream);
        } catch (IOException ex) {
            logger.warn("Could not load flipper settings from "+flipperPropFile);
            ex.printStackTrace();
        }

        // If you want to check templates based on events (i.e. messages on middleware),
        // you can run  flt.forceCheck(); from a callback to force an immediate check.
        /*logger.debug*/System.out.println("FlipperLauncher: Starting Thread");
        flt = new FlipperLauncherThread(ps);
        flt.start();
	}
        
	public static void main(String[] args) {
       
		String help = "Expecting commandline arguments in the form of \"-<argname> <arg>\".\nAccepting the following argnames: config";
    	String flipperPropFile = "flipper.properties";
        String flipperPropertyRes = "C:\\Mukesh\\Fun\\Flipper2.0.1.0\\FlipperLib\\src\\resource/couchflipper.properties";
    	
     /*   if(args.length % 2 != 0){
        	System.err.println(help);
        	System.exit(0);
        }
        
        for(int i = 0; i < args.length; i = i + 2){
        	if(args[i].equals("-config")) {
        		flipperPropFile = args[i+1];
        	} else {
            	System.err.println("Unknown commandline argument: \""+args[i]+" "+args[i+1]+"\".\n"+help);
            	System.exit(0);
        	}
        }*/
        
        InputStream inputstream = null;
            try {
                inputstream = new FileInputStream(flipperPropertyRes);
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(FlipperLauncher.class.getName()).log(Level.SEVERE, null, ex);
            }
        Properties ps = new Properties();
      //  InputStream flipperPropStream = FlipperLauncher.class.getClassLoader().getResourceAsStream(flipperPropFile);

        try {
           // ps.load(flipperPropStream);
              ps.load(inputstream);
        } catch (IOException ex) {
            logger.warn("Could not load flipper settings from "+flipperPropFile);
            ex.printStackTrace();
        }

        // If you want to check templates based on events (i.e. messages on middleware),
        // you can run  flt.forceCheck(); from a callback to force an immediate check.
        /*logger.debug*/System.out.println("FlipperLauncher: Starting Thread");
        flt = new FlipperLauncherThread(ps);
        flt.start();
	}        
        
}

/* Sample flipper.properties file:

# name for TemplateController
name: TestTest
# comma seperated list of external lists
jslibs: jslibs/underscore-min.js
# comma seperated list of template paths
templates: example/Underscore.xml
#evaluate template every 1000/evalFrequency milliseconds
evalFrequency: 1
#n=0:don't stop, n=1+: stop after n times checking templates
maxSteps: 0
#True|False
stopIfUnchanged: False
#ALL,NONE,ONLY_CHANGED
autoReloadTemplates: ONLY_CHANGED

*/
