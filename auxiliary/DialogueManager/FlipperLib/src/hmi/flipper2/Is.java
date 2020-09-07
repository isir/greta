package hmi.flipper2;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import hmi.flipper2.javascript.JsEngine;
import hmi.flipper2.javascript.JsExpression;
import hmi.flipper2.postgres.Database;

public class Is extends JsEngine {
	
	private Database db;
	private Map<String, TemplateFile> is_tf_table;
	
	Is(TemplateController tc, Database db) throws FlipperException {
		super(tc);
		this.db = db;
		is_init();
	}
	
	public Database getDatabase() {
		if (this.db != null )
			return this.db;
		else
			return null;
	}
	
	public Connection getDbConnection() {
		if (this.db != null )
			return this.db.getConnection();
		else
			return null;
	}
	
	private void is_init() throws FlipperException {
		this.is_tf_table = new HashMap<String, TemplateFile>();
		execute("var is = { tag : \"Information State\" }");
		initPersistentObjects();
	}
	
	public void commit() throws FlipperException {
		if (this.db != null) {
			for (Map.Entry<String, TemplateFile> entry : this.is_tf_table.entrySet()) {
				if (entry.getValue().is_updated) {
					db.updateTemplateFileIs(entry.getValue(), this.getJSONfromJs("is."+entry.getKey()));
					entry.getValue().is_updated = false;
				}
			}
		}
	}
	
	public void rollback() throws FlipperException {
		if (this.db != null) {
			this.db.rollback();
			for (Map.Entry<String, TemplateFile> entry : this.is_tf_table.entrySet()) {
				if (entry.getValue().is_updated) {
					entry.getValue().is_updated = false;
					System.out.println("INCOMPLETE:Is:rollback: should re-read is after db rollback");
				}
			}
		}
	}
	
	private static final String extract_is(String is_var) throws FlipperException {
		String[] steps = is_var.split("\\.");
		if ( steps.length < 2 || !steps[0].equals("is"))
			throw new FlipperException("Is:assign:bad variable: "+is_var);
		return steps[1];
	}
	
	public TemplateFile tf_from_is_var(String is_var) {
		try {
			String is_tfid = extract_is(is_var);
			return is_tf_table.get(is_tfid);
		} catch (FlipperException e) {
			return null;
		}
	}
	
	private void registerUpdate(String is_var) throws FlipperException {
		String is_tfid = extract_is(is_var);
		TemplateFile tf = is_tf_table.get(is_tfid);
		if ( tf == null )
			throw new FlipperException("Is:assign:unkown is: "+is_tfid);
		if ( db != null )
			tf.is_updated = true;
	}
	
	public void assignJsExpression(String is_var, JsExpression js_expr) throws FlipperException {
		registerUpdate(is_var);
		js_expr.eval_void();
	}
	
	public void assignDerefJavascript(String is_var, String js_expr) throws FlipperException {
		//do we need to dereference the variable, i.e., replace the path with the value of that state variable?
		if (!is_var.startsWith("*"))
			throw new RuntimeException("UNEXPECTED");
		is_var = getIs(is_var.substring(1));
		is_var = is_var.substring(1, is_var.length() - 1);
		registerUpdate(is_var);
		execute(is_var + " = " + js_expr);
	}
	
	public void assignJSONString(String is_var, String json_expr) throws FlipperException {
		//do we need to dereference the variable, i.e., replace the path with the value of that state variable?
        if (is_var.startsWith("*"))
        {
            is_var=getIs(is_var.substring(1));
            is_var=is_var.substring(1,is_var.length()-1);
        }
		registerUpdate(is_var);
		assignJSONtoJs(is_var, json_expr);
	}
	
	public String getIs(String path) throws FlipperException {
		return this.getJSONfromJs(path);
	}
	
	public void activate_tf(TemplateFile tf, String json_value) throws FlipperException {
		assignJSONtoJs("is."+tf.is_name, json_value);
		is_tf_table.put(tf.is_name, tf);
	}
	
	public void deactivate_tf(TemplateFile tf) throws FlipperException {
		assignJSONtoJs("is."+tf.is_name, "{}");
		is_tf_table.remove(tf.is_name);
	}
	
	public enum ValueTransferType { TYPE_OBJECT, TYPE_JSONSTRING};
	
	public static ValueTransferType transferType(String s) throws FlipperException {
		if ( s == null )
			return ValueTransferType.TYPE_OBJECT;
		else if ( s.toLowerCase().equals("object")) {
			return ValueTransferType.TYPE_OBJECT;
		} else if ( s.toLowerCase().equals("jsonstring")) {
			return ValueTransferType.TYPE_JSONSTRING;
		} else
			throw new FlipperException("ValueTransferType: unknown is_type: "+ s);
	}
	
	/*
	 * The Persistent Objects are part of Is at the moment because this is the most logical at the moment!
	 * INCOMPLETE: transaction management
	 */
	
	private Hashtable<String, Object> persistentObjects;
	
	private void initPersistentObjects() {
		this.persistentObjects = new Hashtable<String, Object>();
	}
	
	public void putPersistent(Template template, String s, Object o) {
		this.persistentObjects.put(s, o);
	}
	
	public Object getPersistent(String s) {
		return this.persistentObjects.get(s);
	}
	
}

