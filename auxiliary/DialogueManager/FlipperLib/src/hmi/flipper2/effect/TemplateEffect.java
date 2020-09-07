package hmi.flipper2.effect;

import java.util.Set;

import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.dataflow.DataFlow;

public class TemplateEffect extends Effect {

	private String regexpr;
	private String isregexpr;
	public TemplateEffect(String id, String regexpr, String isregexpr) throws FlipperException {
		super(id);
		if ( regexpr != null && isregexpr != null )
			throw new FlipperException("TemplateEffect cannot have both regexpr and isregexpr");
		this.regexpr	= regexpr;
		this.isregexpr	= isregexpr;
		// System.out.println("regexpr="+this.regexpr);
		// System.out.println("isregexpr="+this.isregexpr);
	}
	
	@Override
	public Object doIt(Is is) throws FlipperException {
		if ( this.isregexpr != null ) {
			this.regexpr = is.getIs(this.isregexpr);
			if ( this.regexpr == null )
				throw new FlipperException("checktemplates: isregexpr not found: "+this.isregexpr);
			else 
				this.regexpr = this.regexpr.substring(1, this.regexpr.length()-1); // strip quotes
		}
		is.tc.checkConditionalTemplates(this.regexpr);
		return null;
	}
	
	public Set<String> flowIn() {
		return DataFlow.EMPTY;
	}
	
	public Set<String> flowOut() {
		return DataFlow.EMPTY;
	}

}
