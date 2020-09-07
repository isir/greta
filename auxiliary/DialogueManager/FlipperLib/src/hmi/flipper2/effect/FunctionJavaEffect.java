package hmi.flipper2.effect;

import hmi.flipper2.FlipperException;
import hmi.flipper2.Template;
import hmi.flipper2.value.JavaValueList;

public class FunctionJavaEffect extends JavaEffect {
	
	public FunctionJavaEffect(String id, Template template, String is_assign, String is_type, String className, String functionName, JavaValueList arguments)
			throws FlipperException {
		super(id, template, is_assign, is_type, className, null, new JavaValueList(), functionName, arguments, CallMode.CALL_FUNCTION, ObjectMode.OBJECT_SINGLE);
	}
	
}