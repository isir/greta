package hmi.flipper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import javax.swing.filechooser.FileSystemView;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExperimentFileLogger {
	
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ExperimentFileLogger.class.getName());
	
	private static ExperimentFileLogger INSTANCE = null;
	
	private Path session = null;
	private ObjectMapper om = null;
	
	private ExperimentFileLogger() {
		om = new ObjectMapper();
	}
	
	public static ExperimentFileLogger getInstance()  { 
        if (INSTANCE == null) 
        	INSTANCE = new ExperimentFileLogger(); 
        return INSTANCE; 
    }
	
	
	public synchronized void newSession() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_HHmmss");
		Date dt = new Date();
		String session_name = sdf.format(dt);
		FileSystemView filesys = FileSystemView.getFileSystemView();
		session = Paths.get(filesys.getHomeDirectory().getAbsolutePath(), "BORGLOGS", session_name);
		File directory = session.toFile();
	    if (!directory.exists()){
	        directory.mkdirs();
	    }  
	}



	public synchronized void log(String streamId, JsonNode jn) {
		try {
			String logString = om.writeValueAsString(jn);
			log(streamId, logString, false);
		} catch (Exception e) {
			logger.warn("failed to log json...", e);
		}
	}
	
	public synchronized void log(String streamId, String data) {
		log(streamId, data, true);
	}
	
	private synchronized void log(String streamId, String data, boolean encode) {
		File file = getFilePath(streamId).toFile();
		FileWriter fr;
		try {
			fr = new FileWriter(file, true);
			BufferedWriter br = new BufferedWriter(fr);
			br.write(""+System.currentTimeMillis());
			br.write("\t");
			if (encode) {
				String encodedData = Base64.getEncoder().encodeToString(data.getBytes());
				br.write(encodedData);
			} else {
				br.write(data);
			}
			br.write("\n");
			br.close();
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private synchronized Path getFilePath(String streamId) {
		if (session == null) {
			newSession();
		}
		return Paths.get(session.toString(), streamId+".txt");
	}

}
