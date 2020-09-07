package hmi.flipper2.conditions;

import java.util.HashSet;
import java.util.Set;

import hmi.flipper2.Config;
import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.javascript.JsExpression;

public class JsCondition extends Condition {

	private JsExpression condition;
	
	public JsCondition(String id, JsExpression condition) {
		super(id);
		this.condition = condition;
	}
	
	@Override
	public boolean checkIt(Is is) throws FlipperException {
		return condition.eval_boolean();
	}
	
	public Set<String> flowIn() {
		return condition.extractRefs();
	}

}
