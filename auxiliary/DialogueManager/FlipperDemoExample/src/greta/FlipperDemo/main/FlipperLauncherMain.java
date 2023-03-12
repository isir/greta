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




public class FlipperLauncherMain {

    private static Logger logger = LoggerFactory.getLogger(FlipperLauncherMain.class.getName());
    private static FlipperLauncherThread flt;
    
    private static FlipperLauncherMain singleToneInstance;
 
       
    	private String host = null;
	private String port = null;
	private String gretaASRTopic = null;
	private String gretaInputTopic = null;
	private String flipperPropertyRes = System.getProperty("user.dir")+"\\Common\\Data\\FlipperResources\\flipperDemo.properties";
  	private String flipperTemplateFolderPath = System.getProperty("user.dir")+"\\Common\\Data\\FlipperResources\\fmltemplates";      
	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return this.port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getGretaASRTopic() {
		return this.gretaASRTopic;
	}
	
	public String getGretaInputTopic() {
		return this.gretaInputTopic;
	}

	public void setGretaASRTopic(String requestTopic) {
		this.gretaASRTopic = requestTopic;
	}
	
	public void setGretaInputTopic(String responseTopic) {
		this.gretaInputTopic = responseTopic;
	}


	public String getFlipperPropertyResource() {
		return this.flipperPropertyRes;
	}

	public String getflipperTemplateFolderPath() {
		return this.flipperTemplateFolderPath;
	}
        
        
       public void setActiveMqParameters(String host, String port, String gretaAsrTopic, String gretaInputTopic){
          this.host= host;
          this.port= port;
          this.gretaASRTopic = gretaAsrTopic;
          this.gretaInputTopic = gretaInputTopic;
      }
    
    public static FlipperLauncherMain getInstance() {
        if(singleToneInstance == null) {
            singleToneInstance = new FlipperLauncherMain();
        }
        
        return singleToneInstance;
    }
    
  
    public FlipperLauncherMain(){
      singleToneInstance = this;
       // init();
    }
   public void init(){
        String help = "Expecting commandline arguments in the form of \"-<argname> <arg>\".\nAccepting the following argnames: config";
        
        Properties ps = new Properties();
         InputStream inputstream = null;
         System.out.println("greta.FlipperDemo.main.FlipperLauncherMain.init()"+"   "+ System.getProperty("user.dir")+"\\Common\\Data\\FlipperResources\\flipperDemo.properties");
        try {
            inputstream = new FileInputStream(flipperPropertyRes);
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(FlipperLauncherMain.class.getName()).log(Level.SEVERE, null, ex);
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
