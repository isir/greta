package hmi.flipper2.conditions;

import java.util.ArrayList;
import java.util.Set;

import hmi.flipper2.Config;
import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.dataflow.DataFlow;

public class ConditionList extends ArrayList<Condition> implements DataFlow {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	boolean andMode;
	
	public boolean lastCheck;
	
	public ConditionList(String mode) throws FlipperException {
		if (mode == null) 
			andMode = true;
		else if (mode.equals("and"))
			andMode = true;
		else if (mode.equals("or"))
			andMode = false;
		else
			throw new FlipperException("ConditionList: unknown mode: "+mode);
	}
	
	private final boolean mark(boolean v) {
		this.lastCheck = v;
		return v;
	}
	
	private static final boolean checkOne(Condition c, Is is) throws FlipperException {
		boolean res;
		
		if ( Config.debugging && is.tc.dbg != null )
			is.tc.dbg.start_Precondition(c.id(), c.toString());
		res = c.checkIt(is);
		if ( Config.debugging && is.tc.dbg != null )
			is.tc.dbg.stop_Precondition(c.id(), res+"");
		return res;
	}
	
	public boolean checkIt(Is is) throws FlipperException {
		if ( andMode ) {
			for(Condition c: this)
				if ( !checkOne(c, is) )
					return mark(false);
			return mark(true);
		} else {
			for(Condition c: this)
				if ( checkOne(c, is) )
					return mark(true);
			return mark(false);
		}
	}
	
	public Set<String> flowIn() {
		Set<String> res = DataFlow.EMPTY;
		for(Condition c: this)
			res = DataFlow.union(res, c.flowIn());
		return res;
	}
	
	public Set<String> flowOut() {
		Set<String> res = DataFlow.EMPTY;
		for(Condition c: this)
			res = DataFlow.union(res, c.flowOut());
		return res;
	}
	
}
	