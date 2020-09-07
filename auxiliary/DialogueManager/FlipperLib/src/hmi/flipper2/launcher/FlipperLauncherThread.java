package hmi.flipper2.launcher;

import hmi.flipper2.Config;
import hmi.flipper2.FlipperException;
import hmi.flipper2.TemplateController;
import hmi.flipper2.debugger.FlipperDebugger;
import hmi.flipper2.postgres.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class FlipperLauncherThread extends Thread {
	
	private enum AutoReloadBehavior {
	    NONE, ALL, ONLY_CHANGED 
	}
	
	private static Logger logger = LoggerFactory.getLogger(FlipperLauncherThread.class.getName());

	// Settings
	private boolean forceCheck;
	private boolean stop;
	private String name;
	private String[] jslibs;
	private String[] templates;
	private double evalFrequency;
	private int maxSteps;
	private boolean stopIfUnchanged;
	private AutoReloadBehavior autoReloadTemplates;
	// Database
	private Database db;
	private String host;
	private String database;
	private String role;
	private String password;
	private boolean analyze;

	private ArrayList<FlipperTemplateWatcher> watchedTemplates;
	
	protected TemplateController tc;
	
	public FlipperLauncherThread() {
		this(null);
	}
	
	public FlipperLauncherThread(Properties ps) {
		this.stop = false;
		this.db = null;
		this.watchedTemplates = new ArrayList<FlipperTemplateWatcher>();
		
		if (ps == null) {
			init(new Properties()); // defaults
		} else {
			init(ps);
		}
	}
	
	private void init(Properties ps) {
		ArrayList<String> _jslibs = new ArrayList<>();
		for (String path : ps.getProperty("jslibs", "").split(",")) {
			if (path.trim().length() > 0) _jslibs.add(path.trim());
		}
		ArrayList<String> _templates = new ArrayList<>();
		for (String path : ps.getProperty("templates", "").split(",")) {
			if (path.trim().length() > 0) _templates.add(path.trim());
		}

		// Settings
		this.name = ps.getProperty("name", "flippers");
		this.templates = _templates.toArray(new String[0]);
		this.jslibs = _jslibs.toArray(new String[0]);
		this.evalFrequency = Double.parseDouble(ps.getProperty("evalFrequency", "1.0"));
		this.maxSteps = Integer.parseInt(ps.getProperty("maxSteps", "0"));
		this.stopIfUnchanged = Boolean.parseBoolean(ps.getProperty("stopIfUnchanged", "False"));
		// Database
		this.host = ps.getProperty("host", "");
		this.database = ps.getProperty("database", "");
		this.role = ps.getProperty("role", "");
		this.password = ps.getProperty("password", "");
		// Analyze
		this.analyze = Boolean.parseBoolean(ps.getProperty("analyze","False"));
		
		try {
			this.autoReloadTemplates = AutoReloadBehavior.valueOf(ps.getProperty("autoReloadTemplates", "NONE"));
		} catch (IllegalArgumentException e) {
			this.autoReloadTemplates = AutoReloadBehavior.NONE;
		}
		
		try {
			if (!host.equals("") && !database.equals("") && !role.equals("")) {
				this.db = new Database("jdbc:postgresql://"+host+"/"+database, role, password);
				if(db.getConnection().isValid(0)){
                    logger.info("Database connected to: " +host+"/"+database);
                }
				db.clearAll();
			}
			tc = TemplateController.create(name, "", db, jslibs);
			if(Config.debugging){
			    tc.setDebugger(new FlipperDebugger(tc));
            }
			for (String templatePath : templates) {
				//String resourcePath = tc.resourcePath(templatePath);
				//tc.addTemplateFile(resourcePath);				
                                tc.addTemplateFile(templatePath);
				/*logger.debug*/System.out.println("Added template: "+templatePath);
				if (!autoReloadTemplates.equals(AutoReloadBehavior.NONE)) {
					FlipperTemplateWatcher watcher = new FlipperTemplateWatcher(templatePath);
					watchedTemplates.add(watcher);
					watcher.start();
				}
			}
			if(this.analyze){
				logger.info("Analyzing dataflow");
				tc.dataflow.analyze();
			}
			if(db != null){
			    db.commit();
            }
			LogIS(0);
		} catch (FlipperException e) {
			e.extra += "\nFlipperLauncherThread: setup failed";
			stop = true;
			FlipperException.handle(e);
		} catch (SQLException e) {
            e.printStackTrace();
        }
    }
	
	public void run() {
		int steps = 0;
		long lastEval = 0;
		
		while (!stop) {
			
			if (handleChangedTemplates()) { // If something changed...
				steps = 0;
				LogIS(steps);
				forceCheck();
			}
			
			
			if (forceCheck || evalFrequency == 0 || lastEval + (long) 1000/evalFrequency < System.currentTimeMillis()) {
				boolean changed = false;
				try { // Do the work
					changed = tc.checkTemplates();
					steps++;
					LogIS(steps);
				} catch (FlipperException e) {
					e.extra = "FlipperLauncherThread: evaluating template failed";
					FlipperException.handle(e);
					stop = true;
				}
				
				// Check whether/when to continue
				if (maxSteps > 0 && steps >= maxSteps) stop = true;
				if (stopIfUnchanged && !changed) stop = true;
				if (evalFrequency > 0.0) {
					try {
						Thread.sleep((long) (1000/evalFrequency));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				forceCheck = false;
				lastEval = System.currentTimeMillis();
			}
		}

		try {
			TemplateController.destroy(name, db);
			if (tc != null) tc.close();
		}  catch (FlipperException e) {
			e.extra = "FlipperLauncherThread: destroying db conn failed";
			FlipperException.handle(e);
		}
    }
	
	public void stopGracefully() {
		stop = true;
	}
	
	public void forceCheck() {
		forceCheck = true;
	}
	
	public void LogIS(int stepMarker) {
		try {
			/*logger.debug*/System.out.println("\nIS @ "+stepMarker+":\n---\n"+tc.getIs("is")+"\n");
		} catch (FlipperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean handleChangedTemplates() {

		if (autoReloadTemplates.equals(AutoReloadBehavior.NONE))
			return false;
		
		ArrayList<String> resetFiles = new ArrayList<String>();
		
		for (int i = 0; i < watchedTemplates.size(); i++) {
			if (watchedTemplates.get(i).hasChanged()) {
				watchedTemplates.get(i).reset();
				resetFiles.add(watchedTemplates.get(i).getTemplatePath());
			}
		}
		
		if (resetFiles.size() > 0 && autoReloadTemplates.equals(AutoReloadBehavior.ALL)) {
			try {
				TemplateController.destroy(name, db);
				tc.close();
				tc = TemplateController.create(name, "", db, jslibs);
				for (FlipperTemplateWatcher tw : watchedTemplates) {
					tc.addTemplateFile(tw.getTemplatePath());
				}
			} catch (FlipperException e) {
				e.extra = "FlipperLauncher: Failed to reset TemplateController";
				FlipperException.handle(e);
			}
		} else if (resetFiles.size() > 0 && autoReloadTemplates.equals(AutoReloadBehavior.ONLY_CHANGED)) {
			for (String p : resetFiles) {
				try {
					tc.removeTemplateFile(p);
				} catch (FlipperException e) {
					e.extra = "FlipperLauncher: Failed to remove template on change";
					FlipperException.handle(e);
				}

				try {
					tc.addTemplateFile(p);
				} catch (FlipperException e) {
					e.extra = "FlipperLauncher: Failed to add template after change";
					FlipperException.handle(e);
				}
			}
		}
		return resetFiles.size() > 0;
	}
	
}
