package hmi.flipper2.debugger;

import hmi.flipper2.TemplateController;

public class FlipperDebugger {
	
	enum Kind {
		Start, Stop
	};
	
	enum Action {
		CheckTemplates, CheckTemplate, Precondition, Effect, JavascriptExec, JavaExec, Other
	};
	
	protected long totalTimes[] = null;
	
	private FlipperState state = null;
	
	public void handle_state(FlipperState s) {
		boolean verbose = false;
		
		if ( verbose )
			System.out.println("#"+s.action.name() + "\t" + s.id + "\t"+pt(s.duration()));
		switch (s.action) {
		case CheckTemplates: {
			System.out.println("[");
			for (Action a : Action.values()) {
				System.out.println("\t+ Total Time: "+ String.format("%1$14s", a.name()) + " = " + pt(this.totalTimes[a.ordinal()]));
			}
			System.out.println("]");
		}
			break;
		case CheckTemplate:
			break;
		case Precondition:
			break;
		case Effect:
			break;
		case JavascriptExec:
			break;
		case JavaExec:
			break;
		case Other:
			throw new RuntimeException("Other actions Not Implemented Yet");
		}
	}
	
	private void handle_startstop(Kind kind, Action action, String other_action, String id, String arg) {
		boolean verbose = true;
		
		if ( kind == Kind.Start ) {
			state = state.push();
			state.action = action;
			state.id = id;
			state.startTime = System.nanoTime();
			state.startArg = arg;
			if ( verbose )
				System.out.println(kind.name() + "\t" + String.format("%1$14s", action.name()) + "\t" + id);
		} else {
			state.stopTime = System.nanoTime();
			state.stopArg = arg;
			if ( verbose )
				System.out.println(kind.name() + "\t" + String.format("%1$14s", action.name())  + "\t" + id + "\t("+pt(state.duration())+")");
			this.totalTimes[action.ordinal()] += state.duration();
			handle_state(state);
			state = state.pop();
		}
				
	}
	
	public void start_CheckTemplates(String id, String arg) {
		this.handle_startstop(Kind.Start, Action.CheckTemplates, null, id, arg);
	}
	
	public void stop_CheckTemplates(String id, String arg) {
		this.handle_startstop(Kind.Stop, Action.CheckTemplates, null, id, arg);
	}
	
	public void start_CheckTemplate(String id, String arg) {
		this.handle_startstop(Kind.Start, Action.CheckTemplate, null, id, arg);
	}
	
	public void stop_CheckTemplate(String id, String arg) {
		this.handle_startstop(Kind.Stop, Action.CheckTemplate, null, id, arg);
	}
	
	public void start_Precondition(String id, String arg) {
		this.handle_startstop(Kind.Start, Action.Precondition, null, id, arg);
	}
	
	public void stop_Precondition(String id, String arg) {
		this.handle_startstop(Kind.Stop, Action.Precondition, null, id, arg);
	}
	
	public void start_Effect(String id, String arg) {
		this.handle_startstop(Kind.Start, Action.Effect, null, id, arg);
	}
	
	public void stop_Effect(String id, String arg) {
		this.handle_startstop(Kind.Stop, Action.Effect, null, id, arg);
	}
	
	public void start_JavascriptExec(String id, String arg) {
		this.handle_startstop(Kind.Start, Action.JavascriptExec, null, id, arg);
	}
	
	public void stop_JavascriptExec(String id, String arg) {
		this.handle_startstop(Kind.Stop, Action.JavascriptExec, null, id, arg);
	}
	
	public void start_JavaExec(String id, String arg) {
		this.handle_startstop(Kind.Start, Action.JavaExec, null, id, arg);
	}
	
	public void stop_JavaExec(String id, String arg) {
		this.handle_startstop(Kind.Stop, Action.JavaExec, null, id, arg);
	}

	public static final String pt(long t) {
		if ( t != (long)-1 )
			return ((int) (t / 1000)) + "us";
		else 
			return "EMPTY";
	}

	protected TemplateController tc;

	public FlipperDebugger(TemplateController tc) {
		this.tc = tc;
		start();
	}
	
	private void start() {
		this.state = FlipperState.create(8);
		this.totalTimes = new long[Action.values().length];
		for (Action a : Action.values() ) {
			this.totalTimes[a.ordinal()] = 0;
		}
	}
	
	public void restart() {
		start();
	}
	
}
