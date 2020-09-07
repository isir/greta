package hmi.flipper2.launcher;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.nio.file.SensitivityWatchEventModifier;

public class FlipperTemplateWatcher extends Thread {
	
	private static Logger logger = LoggerFactory.getLogger(FlipperTemplateWatcher.class.getName());
	
	private boolean stop;
	private boolean fileChanged;
	private String pathString;
	private boolean paused;
	
	private final Object pauseLock = new Object();
	
	public FlipperTemplateWatcher(String p) {
		this.pathString = p;
	}
	
	public String getTemplatePath() {
		return pathString;
	}
	
	public boolean hasChanged() {
		return fileChanged;
	}

	public void stopGracefully() {
		stop = true;
	}
	
	public void reset() {
		paused = false;
    	fileChanged = false;
	}
	
	public void run() {
		final Path path = FileSystems.getDefault().getPath(pathString);
		final String watchedFileName = path.getFileName().toString();
		stop = false;
		paused = false;
		fileChanged = false;
		/*logger.debug*/System.out.println("Watching template: "+watchedFileName);
		try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
			path.getParent().register(watchService, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
			final WatchKey wk = watchService.take();
		    while (!stop) {
		    	if (paused) continue;
		    	
		    	if (!wk.reset()) {
		    		/*logger.debug*/System.out.println("WatchKey invalid, stoped watching "+pathString);
		    		stop = true;
		        }
		    	
		        for (WatchEvent<?> event : wk.pollEvents()) {
		            final Path changed = (Path) event.context();
		            if (changed.equals(path.getFileName())){
			    		/*logger.debug*/System.out.println("Changed ");
		            	fileChanged = true;
		            	paused = true;
		            }
		        }
		    }
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		/*logger.debug*/System.out.println("==== ENDED WATCHER =====");
	}
}

