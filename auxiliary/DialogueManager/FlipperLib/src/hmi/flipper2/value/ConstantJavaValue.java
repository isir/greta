package hmi.flipper2.value;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import hmi.flipper2.FlipperException;

public class ConstantJavaValue extends JavaValue {

	private String		class_str; // constructor is String Object, class must have String Constructor
	private Class<?>	class_obj;
	private String 		str_value;
	private Object		obj_value;
	
	public ConstantJavaValue(String class_str, String str_value) throws FlipperException {
		this.class_str = class_str;
		this.str_value = str_value;
		if ( this.class_str == null )
			this.class_obj = this.str_value.getClass();
		else
			this.class_obj = name2class(this.class_str);	
		obj_value = convertString2Object(this.class_obj, this.str_value);
	}
	
	private static final Class<?> stringClassList[] = {String.class};
	
	private static Object convertString2Object(Class<?> c, String s) throws FlipperException {
		try {
			Constructor<?> dynConstructor = c.getConstructor(stringClassList);
			return dynConstructor.newInstance(s);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new FlipperException(e);
		}
	}
	
	
	@Override
	public Object getObject() throws FlipperException {
		return obj_value;
	}
	
	@Override
	public Class<?> objectClass() throws FlipperException {
		return this.class_obj;
	}

}
