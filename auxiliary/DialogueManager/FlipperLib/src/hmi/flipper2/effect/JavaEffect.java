package hmi.flipper2.effect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import hmi.flipper2.Config;
import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.Is.ValueTransferType;
import hmi.flipper2.dataflow.DataFlow;
import hmi.flipper2.Template;
import hmi.flipper2.TemplateController;
import hmi.flipper2.value.JavaValueList;

public class JavaEffect extends Effect {

	public enum CallMode { CALL_METHOD, CALL_FUNCTION };
	
	protected static final String cm2string(CallMode cm) {
		return (cm==CallMode.CALL_METHOD) ? "METHOD" : "STATIC FUNCTION";
		
	}
	
	public enum ObjectMode { OBJECT_SINGLE, OBJECT_MULTI };
	
	protected static final String om2string(ObjectMode om) {
		return (om==ObjectMode.OBJECT_SINGLE) ? "SINGLE" : "MULTI";
		
	}
	
	protected static final Object[] emptyArgs = {};
	protected static final Class<?> emptyClassArgs[] = {};
	
	protected Template	template;
	protected String	is_assign;
	protected String 	className;
	protected String	persisten;
	protected JavaValueList 	constructors;
	protected String 	persistent = null;
	protected String 	callName;
	protected JavaValueList 	arguments;
	protected CallMode 	callmode;
	protected ObjectMode objectmode;
	
	protected ValueTransferType vt_type;
	
	protected Class<?>		classObject = null;
	protected Class<?>[]	paramTypes = null;
	protected Method		callMethod = null;
	protected Object		callObject = null;
	
	
	public JavaEffect(String id, Template template, String is_assign, String is_type, String className, String persistent, JavaValueList constructors, String callName, JavaValueList arguments, CallMode callMode,
			ObjectMode objectMode) throws FlipperException {
		super(id);
		try {
			this.template = template;
			this.is_assign = is_assign; 		
			this.vt_type = Is.transferType(is_type);
			this.className = className;
			this.persistent = persistent;
			this.constructors = constructors;
			this.callName = callName;
			this.arguments = (arguments != null) ? arguments : new JavaValueList();
			this.callmode = callMode;
			this.objectmode = objectMode;
			//
			this.classObject = Class.forName(this.className);
			this.paramTypes = this.arguments.classArray();
			if ( this.callName != null )
				this.callMethod = this.classObject.getMethod(this.callName, this.paramTypes);
			else
				this.callMethod = null; // just create an object
			if (this.callmode == CallMode.CALL_METHOD) {
				if (this.objectmode == ObjectMode.OBJECT_SINGLE)
					this.callObject = null;
			} else {
				this.callObject = this.classObject;
			}
		} catch (NoSuchMethodException e) {
			throw new FlipperException(e, "!Cannot find "+ cm2string(this.callmode) + " " + this.className + " " + this.callName + this.arguments.toString());
		} catch (ClassNotFoundException | IllegalArgumentException | SecurityException e) {
			throw new FlipperException(e);
		}
	}
	
	public boolean isAssign() {
		return this.is_assign != null;
	}
	
	private Object checkPersistent(Object o) {
		if ( this.persistent != null ) {
			this.template.tf.tc.is.putPersistent(template, this.persistent, o);
		}
		return o;
	}
	
	protected Object createObject() throws FlipperException, InstantiationException, IllegalAccessException,
			SecurityException, IllegalArgumentException, InvocationTargetException {
		if ( this.constructors == null ) {
			if ( this.persistent == null )
				throw new FlipperException("!No constructor or persistent spec for: " + " " + this.callName + this.arguments.toString());
			else {
				Object res = this.template.tf.tc.is.getPersistent(this.persistent);
				if ( res == null )
					throw new FlipperException("Unknown persistent object: "+this.persistent+" in "+this);
				return res;
			}
		} else if (this.constructors.size() == 0) {
			return checkPersistent( this.classObject.newInstance() );
		} else {
			try {
				Class<?> ctypes[] = this.constructors.classArray();
				Constructor<?> dynConstructor = this.classObject.getConstructor(ctypes);
				return checkPersistent( dynConstructor.newInstance(this.constructors.objectArray()) );
			} catch (NoSuchMethodException e) {
				throw new FlipperException(e, "!Cannot find constructor " +className + this.constructors.toString() + " for method " + this.callName);
			}
		}
	}
	
	@Override
	public Object doIt(Is is) throws FlipperException {
		Object method_args[] = this.arguments.objectArray();
		if ( Config.debugging && is.tc.dbg != null )
			is.tc.dbg.start_JavaExec(id(), this.toString());
		Object return_obj =  executeCall(method_args);
		if ( Config.debugging && is.tc.dbg != null )
			is.tc.dbg.stop_JavaExec(id(), null);
		if ( is_assign != null ) {
			if (this.vt_type == ValueTransferType.TYPE_OBJECT ) {
				is.assignObject2Js(is_assign, return_obj);
			} else if (this.vt_type == ValueTransferType.TYPE_JSONSTRING ) {
				is.assignJSONString(is_assign, (String)return_obj);
			} else {
				throw new RuntimeException("UNEXPECTED");
			}
		}
		return return_obj;
	}
	
	protected static final String args2string(Object[] method_args) throws FlipperException {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (Object o : method_args) {
			sb.append(o.getClass().getName());
			sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	protected Object executeCall(Object[] method_args) throws FlipperException {
		try {
			if (this.objectmode == ObjectMode.OBJECT_MULTI || this.callObject == null)
				this.callObject = this.createObject();
			if (this.callMethod != null )
				return this.callMethod.invoke(this.callObject, method_args);
			else
				return null;
		} catch (IllegalAccessException | SecurityException | IllegalArgumentException
				| InvocationTargetException | InstantiationException e) {
			throw new FlipperException(e,
					"JavaCall failed:JavaEffect:executeCall: " + this.toString() + "\n" + "Arguments=" + args2string(method_args));
		}
	}
	
	public String toString() {
		return "JavaEffect["+"name="+this.callName+", className="+this.className+"]";
	}
	
	public Set<String> flowIn() {
		return this.arguments.flowIn();
	}
	
	public Set<String> flowOut() {
		if ( this.isAssign() ) {
			HashSet<String> res = new HashSet<String>();
			res.add(this.is_assign);
			return res;
		} else
			return DataFlow.EMPTY;
	}
	
}
