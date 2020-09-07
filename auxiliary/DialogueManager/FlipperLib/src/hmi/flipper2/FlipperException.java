package hmi.flipper2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import javax.script.ScriptException;

import org.xml.sax.SAXException;

public class FlipperException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Exception ex = null;
	public String text = null;
	public String stack = null;
	public String extra = null;
	
	protected void _init(Exception ex) {
		this.ex = ex;
		this.text = ex.toString();
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bs);
		ex.printStackTrace(ps);
		try {
			this.stack = bs.toString("UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public FlipperException(String text) {
		this.text = text;
	}
	
	public FlipperException(Exception ex) {
		_init(ex);
	}
	
	public FlipperException(Exception ex, String extra) {
		_init(ex);
		this.extra = extra;
	}
	
	public FlipperException(SAXException ex) {
		_init(ex);
	}
	
	public FlipperException(IOException ex) {
		_init(ex);
	}
	
	public FlipperException(SQLException ex) {
		_init(ex);
	}
	
	public FlipperException(SQLException ex, String extra) {
		_init(ex);
		this.extra = extra;
	}
	
	public FlipperException(ScriptException ex, String script) {
		_init(ex);
		this.extra = "JAVASCRIPT ERROR:\n=================\n"+script+"\n";
	}
	
	private String currentInfo = null;
	
	public void registerCurrentTemplate(String current_tf, String current_id, String current_name) {
		this.currentInfo = "!TemplateFile: " + current_tf + " Tid: " + current_id + " Tname: "  + current_name;
	}
	
	public static void handle(FlipperException e) {
		if ( e.extra != null )
			System.err.println(e.extra);
		if ( e.currentInfo != null )
			System.err.println(e.currentInfo);
		System.err.println("!Caught Exception: "+e.text);
		if (e.stack != null )
			System.err.println("!Stack: \n"+e.stack);
	}
	
}
