package hmi.flipper2.effect;

import java.util.HashSet;
import java.util.Set;

import hmi.flipper2.Config;
import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.dataflow.DataFlow;

public class DerefAssignEffect extends Effect {

	private String var;
	private String expr;
	
	public DerefAssignEffect(String id, String var, String expr) {
		super(id);
		this.var = var;
		this.expr = expr;
		
	}
	
	public Object doIt(Is is) throws FlipperException {
		is.assignDerefJavascript(var, expr);
		return null;
	}
	
	public Set<String> flowIn() {
		return DataFlow.extractRefs(this.expr);
	}
	
	public Set<String> flowOut() {
		HashSet<String> res = new HashSet<String>();
		res.add(var);
		return res;
	}
	
}
