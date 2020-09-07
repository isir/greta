package hmi.flipper2.javascript;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import hmi.flipper2.TemplateController;
import hmi.flipper2.debugger.FlipperDebugger;
import hmi.flipper2.Config;
import hmi.flipper2.FlipperException;

public class JsEngine {

	private ScriptEngineManager mgr;
	public  ScriptEngine engine;
	public  Invocable invocable;
	public  TemplateController tc;
	
	public JsEngine(TemplateController tc) throws FlipperException {
		js_init(tc);
	}
	
	protected void js_init(TemplateController tc) throws FlipperException {
		this.mgr = new ScriptEngineManager();
		this.engine = mgr.getEngineByName("nashorn");
		this.invocable = (Invocable)engine;
		this.tc = tc;
	}

	public JsValue execute(String script) throws FlipperException {
		JsValue res = null;

		Object retval = eval(script);
		if (retval != null) {
			res = new JsObjectValue(retval);
		}
		return res;
	}
	
	public Object eval(String script) throws FlipperException {
		try {
			if ( Config.debugging && this.tc.dbg != null )
				this.tc.dbg.start_JavascriptExec("js", script);
			// System.out.println("XXXXXX->"+script);
			Object res = engine.eval(script);
			if ( Config.debugging && this.tc.dbg != null )
				this.tc.dbg.stop_JavascriptExec("js", (res==null)?null:res.toString());
			return res;
		} catch (ScriptException e) {
			int count = 1;
			StringBuffer sb = new StringBuffer();
			for (String line : script.split("\\r?\\n"))
				sb.append(String.format("%3d ", count++) + line + "\n");
			
			if ( Config.debugging && this.tc.dbg != null )
				this.tc.dbg.stop_JavascriptExec("js","ERROR:"+sb.toString());
			throw new FlipperException(e, sb.toString());
		}
	}
	
	public double numericExpression(String js_expr) throws FlipperException {
		Object retval = eval(js_expr);
		if (retval != null) {
			try {
				return ((Number) retval).doubleValue();
			} catch (ClassCastException e) {
			}				
		}
		throw new FlipperException("Expression does not return Number: " + js_expr);
	}
	
	public static final String xfervar = "xfervar";
	
	public void assignRawString(String lhs, String raw) throws FlipperException {
		this.engine.put(xfervar, raw);
		eval(lhs + " = " + xfervar);
	}
	
	public static int JSON_PP_SPACING = 2;
	
	private JsExpression stringify = null;
	
	public String getJSONfromJs(String js_expr) throws FlipperException {
		if (stringify == null) {
			this.stringify = new JsExpression(this, "strxpr",
					"JSON.stringify(eval(" + "strxpr" + "), null, " + JSON_PP_SPACING + ")", "return %s");
		}
		return this.stringify.eval_string(js_expr);
		// return (String)engine.eval("JSON.stringify("+js_expr+", null,
		// "+JSON_PP_SPACING +")");
	}
	
	private JsExpression assignJson = null;
	
//	public void assignJSONtoJsORG(String var, String json_expr) throws FlipperException {
//		String script = var + "=" + "JSON.parse(" + escapeForJava(json_expr, true) + ")";
//		eval(script);
//	}
	
	public void assignJSONtoJs(String var, String json_expr) throws FlipperException {
		if (assignJson == null) {
			this.assignJson = new JsExpression(this, "jvar,jsonexpr",
					"_jsonexpr=JSON.parse(jsonexpr,true);eval(jvar + '=_jsonexpr;')", "%s");
		}
		this.assignJson.eval_void(var,json_expr);
	}
	
	public void assignObject2Js(String var, Object java_object) throws FlipperException {
		// INCOMPLETE, could be solved with function
		engine.put("xfervalue", java_object);
		eval( var + "=" +"xfervalue");
	}
	
//	private static String escapeForJava( String value, boolean quote )
//	{
//	    StringBuilder builder = new StringBuilder();
//	    if( quote )
//	        builder.append( "\"" );
//	    for( char c : value.toCharArray() )
//	    {
//	        if( c == '\'' )
//	            builder.append( "\\'" );
//	        else if ( c == '\"' )
//	            builder.append( "\\\"" );
//	        else if( c == '\r' )
//	            builder.append( "\\r" );
//	        else if( c == '\n' )
//	            builder.append( "\\n" );
//	        else if( c == '\t' )
//	            builder.append( "\\t" );
//	        else if( c < 32 || c >= 127 )
//	            builder.append( String.format( "\\u%04x", (int)c ) );
//	        else
//	            builder.append( c );
//	    }
//	    if( quote )
//	        builder.append( "\"" );
//	    return builder.toString();
//	}
	
	/*
	 * 
	 * 
	 */
	
//	public static String is = "var is = { \"name\":\"John\", \"age\":30, \"car\":null }";
//	
//	public static void test() {
//		try {
//			ScriptEngineManager mgr = new ScriptEngineManager();
//			ScriptEngine engine = mgr.getEngineByName("JavaScript");
//			// engine.put("a", 41);
//			engine.eval(is);
//			System.out.println(engine.eval("is;").getClass().getName());
//			System.out.println("STR=" + engine.eval("JSON.stringify(is);"));
//			// JSON.parse()
//			// Eerste versie werkt met JSON.stringify en JSON.parse als setting
//			//
//			System.out.println(engine.eval("is[\"name\"];"));
//			engine.eval("var a=43");
//			engine.eval("var cars = [\"Saab\", \"Volvo\", \"BMW\"]");
//			engine.eval("function myFunction() { return cars[2]; }");
//			System.out.println(engine.eval("cars[1]"));
//			System.out.println(engine.eval("myFunction()"));
//		} catch (ScriptException e) {
//			System.err.println("!Caught: " + e);
//		}

//	}
	
//	public static void main(String[] args) {
//		try {
//			System.out.println("XXXXX");
//			ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
//			engine.eval(
//					"var sayHello = function() { print('Hello, ' + 'Piet' + '!'); return 'hello from javascript'; };");
//
//			Invocable invocable = (Invocable) engine;
//
//			Object result = invocable.invokeFunction("sayHello");
//			System.out.println(result);
//			System.out.println(result.getClass());
//
//		} catch (Exception e) {
//			System.out.println("EXCEPTION: " + e);
//		}
//	}
}
