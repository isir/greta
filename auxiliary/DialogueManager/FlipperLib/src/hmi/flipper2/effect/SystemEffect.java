package hmi.flipper2.effect;

import java.util.Set;

import hmi.flipper2.Config;
import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.dataflow.DataFlow;

public class SystemEffect extends Effect {

	private String command;
	private String arg;
	
	public SystemEffect(String id, String command, String arg) {
		super(id);
		this.command = command;
		this.arg = arg;
	}
	
	public Object doIt(Is is) throws FlipperException {
		if ( this.command.equals("debugger") ) {
			if ( arg.toLowerCase().equals("on"))
				is.tc.switchDebugger(true);
			else
				is.tc.switchDebugger(false);
			
		} else if ( this.command.equals("addTemplate") ) {
			throw new RuntimeException("INCOMPLETE");
		} else if ( this.command.equals("rmTemplate") ) {
			throw new RuntimeException("INCOMPLETE");
		} else
			throw new RuntimeException("INCOMPLETE");
		return null;
	}
	
	public Set<String> flowIn() {
		return DataFlow.EMPTY;
	}
	
	public Set<String> flowOut() {
		return DataFlow.EMPTY;
	}
	
}
