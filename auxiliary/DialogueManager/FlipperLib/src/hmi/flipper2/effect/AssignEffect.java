package hmi.flipper2.effect;

import java.util.HashSet;
import java.util.Set;

import hmi.flipper2.Config;
import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.dataflow.DataFlow;
import hmi.flipper2.javascript.JsExpression;

public class AssignEffect extends Effect {

	private String var;
	private String expr;
	private JsExpression js_expr = null;
	
	public AssignEffect(String id, String var, String expr) {
		super(id);
		this.var = var;
		this.expr = expr;		
	}
	
	public Object doIt(Is is) throws FlipperException {
		if ( this.js_expr == null )
			this.js_expr = new JsExpression(is,"",this.var + "=" + expr, "%s");
		is.assignJsExpression(this.var, this.js_expr);
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
