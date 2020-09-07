package hmi.flipper2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import hmi.flipper2.dataflow.DataFlow;
import hmi.flipper2.dataflow.DataFlowManager;
import hmi.flipper2.debugger.FlipperDebugger;
import hmi.flipper2.effect.EffectList;
import hmi.flipper2.postgres.Database;
import hmi.flipper2.sax.SimpleSAXParser;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the main Flipper interface class. You can create/modify/destroy Flipper Templatecontollers using this class. 
 * An example of how to use this class can be found in the Main class 
 * 
 * @author Jan Flokstra
 * @version 1.0
 * 
 */

public class TemplateController {
	
	/**
	 * This method creates a new TemplateController.
	 * 
	 * @param name
	 *            The name of the TemplateController. When db != null this name
	 *            has to be unique .
	 * @param descr
	 *            A description what this Controller does and is used for.
	 * @param db
	 *            The database used by this TemplateController, may be null for
	 *            non-persistent Controller.
	 * @return A running TemplateController. This TemplateController is
	 *         persistent in the database when db != null
	 * @exception FlipperException
	 *                On all errors.
	 */
	public static TemplateController create(String name, String descr, Database db) throws FlipperException {
		if ( db != null ) {
				db.createController(name, descr);
				db.commit();
		}
		return new TemplateController(name, db);
	}
	
	/**
	 * This method creates a new TemplateController.
	 * 
	 * @param name
	 *            The name of the TemplateController. When db != null this name
	 *            has to be unique .
	 * @param descr
	 *            A description what this Controller does and is used for.
	 * @param db
	 *            The database used by this TemplateController, may be null for
	 *            non-persistent Controller.
	 * @return A running TemplateController. This TemplateController is
	 *         persistent in the database when db != null
	 * @exception FlipperException
	 *                On all errors.
	 */
	public static TemplateController create(String name, String descr, Database db, String[] jslibs) throws FlipperException {
		if ( db != null ) {
				db.createController(name, descr);
				db.commit();
		}
		return new TemplateController(name, db, jslibs);
	}
	
	/**
	 * This method destroys a persistent TemplateController.
	 * 
	 * @param name
	 *            The name of the existing TemplateController in the Database.
	 * @param db
	 *            The Database the TemplateController is stored. When db == null the method does nothing.
	 * @param jslibs
	 * 			  String array of additional js libs to preload. 
	 * @exception FlipperException
	 *                On all errors.
	 */
	public static void destroy(String name, Database db) throws FlipperException {
		if ( db != null ) {
				db.destroyController(name);
				db.commit();
		}
	}
	
	/*
	 * 
	 */
	
	private String name;
	private Database db;
	public FlipperDebugger dbg = null;	// the active debugger, when active same as persistent_dbg
	private FlipperDebugger persistent_dbg = null; // the debugger which is persistent but may be inactive
	public int	cid; // controller id in Database
	public List<TemplateFile> tf_list;
	List<Template> all_templates, base_templates, cond_templates;
	// Template conditional_stack = null;
	LinkedList<Template>	conditional_fifo = null;
	public  Is is;
	public DataFlowManager dataflow;
	
	/**
	 * This method constructs a running TemplateController.
	 * 
	 * @param name
	 *            The name of the TemplateController. When db != null the
	 *            TemplateController name has to exist in the Database.
	 *            Otherwise use TemplateController.create().
	 * @param db
	 *            The database used by this TemplateController, may be null for
	 *            a non-persistent Controller.
	 * @exception FlipperException
	 *                On all errors.
	 */
	public TemplateController(String name, Database db) throws FlipperException {
		this.name = name;
		this.db = db;
		this.is = new Is(this, this.db);
		this.dataflow = new DataFlowManager(this);
		if ( this.db != null ) {
			this.cid = db.getControllerID(name);
			this.tf_list = db.getTemplateFiles(this);
		} else {
			this.cid = -1;
			this.tf_list = new ArrayList<TemplateFile>();
		}
		rebuildCheckTemplates();
	}
	
	/**
	 * This method constructs a running TemplateController.
	 * 
	 * @param name
	 *            The name of the TemplateController. When db != null the
	 *            TemplateController name has to exist in the Database.
	 *            Otherwise use TemplateController.create().
	 * @param db
	 *            The database used by this TemplateController, may be null for
	 *            a non-persistent Controller.
	 * @param jslibs
	 * 			  String array of additional js libs to preload. 
	 * @exception FlipperException
	 *                On all errors.
	 */
	public TemplateController(String name, Database db, String[] jslibs) throws FlipperException {
		this(name, db);
		for (String libPath : jslibs) {
                    
                    InputStream libStream = null;
                    try {
                        libStream = new FileInputStream(libPath);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(TemplateController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                   
                    
                    
                    
			//libStream = this.getClass().getClassLoader().getResourceAsStream(libPath);
			if (libStream == null) {
				throw new FlipperException("Cannot find jslib resource in classpath: "+libPath);
			} else {
		        String libCode = new BufferedReader(new InputStreamReader(libStream))
		        		  .lines().collect(Collectors.joining("\n"));
				is.eval(libCode);
			}
		}
	}
	
	/**
	 * These methods sets a debugger object an for a running TemplateController.
	 * Debugger objects must inherit from FlipperDebugger. So a debugger may be set 
	 * the TemplateController which can be switched 'on' by a  <system> tag. 
	 * 
	 * @param fd
	 *            A flipper debugger object or null
	 */
	public void setDebugger(FlipperDebugger fd) {
		setDebugger(fd, true);
	}
	
	public void setDebugger(FlipperDebugger fd, boolean onoff) {
		if ( !Config.debugging && fd!=null )
			System.err.println("WARNING: Setting FlipperDebugger with Config.debugging=false");
		this.persistent_dbg = fd;
		switchDebugger(onoff);
	}
	
	public void switchDebugger(boolean onoff) {
		if ( onoff ) {
			this.dbg = persistent_dbg;
		} else {
			this.dbg = null;
		}
		
	}
	
	/**
	 * This method adds an Xml template file to a running TemplateController.
	 * When the TemplateController is persistent in a Database this template
	 * file is persistently added to the controller.
	 * 
	 * @param path
	 *            The path of the XML template file
	 * @exception FlipperException
	 *                On all errors.
	 */
	public void addTemplateFile(String path) throws FlipperException {
		try {
			try {
				String xml_str = SimpleSAXParser.readFile(path);
				TemplateFile tf = new TemplateFile(this, path, xml_str, null, 0);
				if (this.db != null)
					db.addTemplateFile(this, tf);
				this.tf_list.add(tf);
				addCheckTemplates(tf.templates);
				// now execute the template initialization code
				for(Template t: tf.templates) {
					if ( t.listOfInitializeEffectList.size() > 0 ) {
						t.doInitializeEffects();
					}
				}
			} catch (IOException e) {
				throw new FlipperException(e);
			}
		} catch (FlipperException e) {
			e.registerCurrentTemplate(this.current_tf, this.current_id, this.current_name);
			throw e;
		}
	}
	
	private void addCheckTemplates(List<Template> templates) throws FlipperException {
		for (Template t : templates ) {
			this.all_templates.add(t);
			if ( t.conditional )
				this.cond_templates.add(t);
			else
				this.base_templates.add(t);
		}
	}
	
	public void checkConditionalTemplates(String regexpr) throws FlipperException {
		for (Template t : filterTemplates(this.cond_templates, regexpr) ) {
			// this.conditional_stack = t.push(this.conditional_stack);
			this.conditional_fifo.add(t);
		}
	}
	
	private void rebuildCheckTemplates() throws FlipperException {
		this.all_templates  = new ArrayList<Template>();
		this.base_templates = new ArrayList<Template>();
		this.cond_templates = new ArrayList<Template>();
		for(TemplateFile tf: this.tf_list) 
			addCheckTemplates( tf.templates );
	}
	
	/**
	 * This method removes an Xml template file from a running TemplateController.
	 * When the TemplateController is persistent in a Database this template
	 * file is persistently removed from the controller.
	 * 
	 * @param path
	 *            The path of the XML template file
	 * @exception FlipperException
	 *                On all errors.
	 */
	public void removeTemplateFile(String path) throws FlipperException {
		TemplateFile tf_remove = findTemplateFile(path);
		tf_remove.deactivate();
		if (this.db != null)
			db.removeTemplateFile(this, tf_remove);
		this.tf_list.remove( tf_remove );
		rebuildCheckTemplates();
	}
	
	private TemplateFile findTemplateFile(String path) throws FlipperException {
		for(TemplateFile tf: this.tf_list) {
			if ( tf.path.equals(path) )
				return tf;
		}
		throw new FlipperException("findTemplateFile:not found:"+path);
	}
	
	/**
	 * This method checks all Templates if the preconditions are true and fires
	 * the Effects and Behaviours when necessary. When the controller is
	 * persistent the Information State is saved in the Datababase and the
	 * Database state is committed.
	 * 
	 * @return a boolean value indicating that some Template preconditions were
	 *         true.
	 * @exception FlipperException
	 *                On all errors.
	 */
	public boolean checkTemplates(String templateFilter) throws FlipperException {
		try {
			boolean changed = false;
			// this.conditional_stack = null;
			this.conditional_fifo = new LinkedList<Template>();
			//
			if ( Config.debugging && this.dbg != null ) {
				this.dbg.restart();
				this.dbg.start_CheckTemplates(this.name, null);
			}
			for (Template template : filterTemplates(this.base_templates, templateFilter) ) {
					if ( Config.debugging && this.dbg != null )
						this.dbg.start_CheckTemplate(template.id(), null);
					changed =  checkPreconditions(template, Behaviour.IMMEDIATE_EFFECT) || changed;
//					while ( this.conditional_stack != null ) {
//						Template toCheck = this.conditional_stack;
//						this.conditional_stack = this.conditional_stack.pop();
//						checkPreconditions(toCheck, Behaviour.IMMEDIATE_EFFECT);
//					}
					while ( ! this.conditional_fifo.isEmpty() ) {
						Template toCheck = this.conditional_fifo.remove();
						checkPreconditions(toCheck, Behaviour.IMMEDIATE_EFFECT);
					}
					if ( Config.debugging && this.dbg != null )
						this.dbg.stop_CheckTemplate(template.id(), null);
			}
			if ( ! Behaviour.IMMEDIATE_EFFECT ) {
				for (Template template : this.all_templates) {
					if ( template.preconditions.lastCheck ) {
						executeEffects(template);
					}
				}
			}
			if ( Config.debugging && this.dbg != null )
				this.dbg.stop_CheckTemplates(this.name, null);
			//
			if (changed) {
				is.commit(); // commit the information state
				if (this.db != null)
					this.db.commit();
			}
			return changed;
		} catch (FlipperException e) {
			e.registerCurrentTemplate(this.current_tf, this.current_id, this.current_name);
			throw e;
		}
	}
	
	private List<Template> filterTemplates(List<Template> list, String regexpr) {
		if ( regexpr == null )
			return list;
		else {
			ArrayList<Template> res = new ArrayList<Template>();
			Pattern templatePattern = Pattern.compile(regexpr);
			for (Template t : list )
				if ( templatePattern.matcher(t.id()).matches() ) 
					res.add(t);
			return res;
		}
	}
	
	public boolean checkTemplates() throws FlipperException {
		return checkTemplates(null);
	}
	
	private boolean checkPreconditions(Template t, boolean executeEffectImmediate) throws FlipperException {
		this.registerCurrentTemplate(this.name, t.id(), t.id());
		return t.checkPreconditions(is, executeEffectImmediate);
	}
	
	private void executeEffects(Template t) throws FlipperException {
		this.registerCurrentTemplate(this.name, t.id(), t.id());
		t.executeSelectedEffects(is);
	}
	
	/**
	 * 
	 */
	
	private String current_tf = null;
	private String current_id = null;
	private String current_name = null;
	
	public void registerCurrentTemplate(String current_tf, String current_id, String current_name) {
		this.current_tf = current_tf;
		this.current_id = current_id;
		this.current_name = current_name;
	}
	
	/**
	 * This method returns the String JSON value of the Information State path variable.
	 * 
	 * @param path The Information state variable path as used by Javascript
	 * @return The String JSON Value of the JavaScript Is variable.
	 * @exception FlipperException
	 *                On all errors.
	 */
	public String getIs(String path) throws FlipperException {
		return is.getIs(path);
	}
	
	/**
	 * This method closes a TemplateController. In case of a non-persistent
	 * controller all data will be lost otherwise the controller is persistent
	 * in the Database and can be reopened in the future.
	 * 
	 * @exception FlipperException
	 *                On all errors.
	 */
	public void close() throws FlipperException {
		this.name = null;
		this.db = null;
		this.is = null;
	}
	
	/**
	 * This method returns the absolute filename of a project resource on the host filesystem.
	 * 
	 * @param rpath
	 *            The relative Path of the resource in the project tree.
	 * @return The absolute path of the file in the host filesystem.
	 * @exception FlipperException
	 *                On all errors.
	 */
	public String resourcePath(String rpath) throws FlipperException {
		URL url = this.getClass().getClassLoader().getResource(rpath);
		if ( url == null )
			throw new FlipperException("Resource file: " + rpath + " not found");
                return url.getPath().replaceFirst("^/(.:/)", "$1");
	}
	
}
