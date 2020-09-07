package hmi.flipper2.conditions;

import java.util.Set;

import hmi.flipper2.FlipperException;
import hmi.flipper2.FlipperObject;
import hmi.flipper2.Is;
import hmi.flipper2.dataflow.DataFlow;

public abstract class Condition extends FlipperObject {

	Condition(String id) {
		super(id);
	}
	
	public abstract boolean checkIt(Is is) throws FlipperException;
	
	public Set<String> flowOut() {
		return DataFlow.EMPTY;
	}
	
}
