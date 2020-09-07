package hmi.flipper2.effect;

import java.util.ArrayList;
import java.util.Set;

import hmi.flipper2.Config;
import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.conditions.Condition;
import hmi.flipper2.dataflow.DataFlow;

public class EffectList extends ArrayList<Effect> implements DataFlow {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	boolean weighted = false;
	boolean initialized = false;
	boolean dynamic = false;
	
	public EffectList() {
		this(false);
	}
	
	public EffectList(boolean weighted) {
		this.weighted = weighted;
	}
	
	public EffectList(boolean weighted, boolean dynamic) {
		this.weighted = weighted;
		this.dynamic = dynamic;
	}
	
	public void doIt(Is is) throws FlipperException {
		if (weighted) {
			if ( this.dynamic || !this.initialized ) {
				// pick one at a weighted random
				double total_weight = 0;
				for (Effect eff : this) {
					if (eff.js_weight == null)
						throw new FlipperException("Effect has nog weight in weighted effect List: " + eff.toString());
					eff.computed_weight = is.numericExpression(eff.js_weight);
					total_weight += eff.computed_weight;
				}
				double high_bound = 0.0;
				for (Effect eff : this) {
					double n_weight = eff.computed_weight / total_weight;
					eff.setRandomRange(high_bound, high_bound + n_weight);
					high_bound += n_weight;
				}
				initialized = true;
			}
			double rand = Math.random();
			for (Effect eff : this) {
				if (eff.inRandomRange(rand)) {
					if ( Config.debugging && is.tc.dbg != null )
						is.tc.dbg.start_Effect(eff.id(), eff.toString());	
					eff.doIt(is);
					if ( Config.debugging && is.tc.dbg != null )
						is.tc.dbg.stop_Effect(eff.id(),null);	
					return;
				}
			}
			throw new RuntimeException("UNEXPECTED");
		} else {
			for (Effect eff : this) {
				if ( Config.debugging && is.tc.dbg != null )
					is.tc.dbg.start_Effect(eff.id(), eff.toString());	
				eff.doIt(is);
				if ( Config.debugging && is.tc.dbg != null )
					is.tc.dbg.stop_Effect(eff.id(),null);	
			}
		}
	}
	
	public Set<String> flowIn() {
		Set<String> res = DataFlow.EMPTY;
		for(Effect c: this)
			res = DataFlow.union(res, c.flowIn());
		return res;
	}
	
	public Set<String> flowOut() {
		Set<String> res = DataFlow.EMPTY;
		for(Effect c: this)
			res = DataFlow.union(res, c.flowOut());
		return res;
	}
	
}

