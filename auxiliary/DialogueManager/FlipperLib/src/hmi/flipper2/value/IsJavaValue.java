package hmi.flipper2.value;

import java.util.HashSet;
import java.util.Set;

import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.Is.ValueTransferType;
import hmi.flipper2.dataflow.DataFlow;
import hmi.flipper2.javascript.JsExpression;

public class IsJavaValue extends JavaValue {

	private Is is;
	private String path;	
	private JsExpression js_expr;
	private ValueTransferType vt_type;
	private Class<?> cl;
	
	public IsJavaValue(Is is, String path, String type, String class_str) throws FlipperException {
		this.is = is;
		this.path = path;
		this.js_expr = new JsExpression(is,"",path,"return %s");
		this.vt_type = Is.transferType(type);
		this.cl = (class_str == null)? null : name2class(class_str);
	}
	
	@Override
	public Object getObject() throws FlipperException {
		// INCOMPLETE, should be implemented with JsExpressions
		if ( this.vt_type == ValueTransferType.TYPE_OBJECT ) {
			// Object res = is.eval(path);
			return js_expr.eval_object();
		} else if (this.vt_type == ValueTransferType.TYPE_JSONSTRING )
			return is.getJSONfromJs(path);
		else 
			throw new RuntimeException("UNEXPECTED");
	}

	@Override
	public Class<?> objectClass() throws FlipperException {
		if (this.vt_type == ValueTransferType.TYPE_JSONSTRING)
			return String.class;
		else if (this.cl != null ) {
			// System.out.println("DEFCLASS="+cl.getName());
			return this.cl;
		} else
			throw new RuntimeException("Should define class for is="+path+", is_type: Object. Dynamic calls implemented in future");
	}
	
	public Set<String> flowIn() {
		HashSet<String> res = new HashSet<String>();
		res.add(this.path);
		return res;

	}
}
