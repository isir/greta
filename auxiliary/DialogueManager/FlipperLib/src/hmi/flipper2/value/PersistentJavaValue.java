package hmi.flipper2.value;

import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.Template;

public class PersistentJavaValue extends JavaValue {

	private Is	is;
	private Class<?> classObject;
	private String name;
	
	public PersistentJavaValue(Template template, String className, String name) throws FlipperException {
		this.is = template.tf.tc.is;
		this.name = name;
		//
		if ( className == null )
			throw new FlipperException("PersistentJavaValue: no \"class\" for persistent: "+name);
		try {
			this.classObject = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new FlipperException(e);
		}
	}
	
	private Object tableObject() throws FlipperException {
		Object res = is.getPersistent(this.name);
		
		if ( res == null)
			throw new RuntimeException("PersistentJavaValue: unknown persistent object: "+this.name);
		return res;
	}
	
	@Override
	public Object getObject() throws FlipperException {
		return tableObject();
	}

	@Override
	public Class<?> objectClass() throws FlipperException {
		// return tableObject().getClass();
		return classObject;
	}
}