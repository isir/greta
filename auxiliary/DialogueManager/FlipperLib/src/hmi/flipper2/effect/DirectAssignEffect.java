package hmi.flipper2.effect;

import java.util.HashSet;
import java.util.Set;

import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.dataflow.DataFlow;

public class DirectAssignEffect extends Effect {

	private String var;
	private String stringvalue;
	
	public DirectAssignEffect(String id, String var, String stringvalue) {
		super(id);
		this.var = var;
		this.stringvalue = stringvalue;		
	}
	
	public Object doIt(Is is) throws FlipperException {
		is.assignRawString(this.var, this.stringvalue);
		return null;
	}
	
	public Set<String> flowIn() {
		return DataFlow.EMPTY;
	}
	
	public Set<String> flowOut() {
		HashSet<String> res = new HashSet<String>();
		res.add(var);
		return res;
	}
	
}
