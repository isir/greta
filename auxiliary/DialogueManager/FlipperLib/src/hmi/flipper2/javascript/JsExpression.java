package hmi.flipper2.javascript;

import hmi.flipper2.TemplateController;
import hmi.flipper2.TemplateFile;
import hmi.flipper2.dataflow.DataFlow;

import java.util.HashSet;
import java.util.Set;

import hmi.flipper2.Config;
import hmi.flipper2.FlipperException;

public class JsExpression {

	private static long fcnt = 10000;
	
	private String fid = null;
	private JsEngine jse;
	public  String expr;
	public  TemplateController tc;
	
	public JsExpression(JsEngine jse, String args, String expr, String format) throws FlipperException {
		this.jse = jse;
		this.expr = expr;
		this.fid = "_f" + fcnt++;
		String fundef = "var " + this.fid + " = function(" + args + ") { " + String.format(format, expr) + "; };";
		// System.out.println("FUNDEF: "+fundef);
		jse.eval(fundef);

	}
	
	private Object _eval() throws FlipperException {
		try {
			return jse.invocable.invokeFunction(this.fid);
		} catch (Exception e) {
			throw new FlipperException(e);
		}

	}
	
	private Object _eval(String arg) throws FlipperException {
		try {
			return jse.invocable.invokeFunction(this.fid,arg);
		} catch (Exception e) {
			throw new FlipperException(e);
		}

	}
	
	private Object _eval(String arg1, String arg2) throws FlipperException {
		try {
			return jse.invocable.invokeFunction(this.fid,arg1,arg2);
		} catch (Exception e) {
			throw new FlipperException(e);
		}

	}

	public void eval_void() throws FlipperException {
		_eval();
	}
	
	public Object eval_object() throws FlipperException {
		return _eval();
	}
	
	public void eval_void(String arg1, String arg2) throws FlipperException {
		_eval(arg1,arg2);
	}
	
	public boolean eval_boolean() throws FlipperException {
		Object retval = _eval();
		if (retval != null) {
			try {
				return ((Boolean) retval).booleanValue();
			} catch (ClassCastException e) {
			}				
		}
		throw new FlipperException("Condition not Boolean: " + this.expr);
	}
	
	public String eval_string(String arg) throws FlipperException {
		Object retval = _eval(arg);
		if (retval != null)
			return retval.toString();
		else
			return null;
	}
	
	public Set<String> extractRefs() {
		return DataFlow.extractRefs(this.expr);
	}
	
}
