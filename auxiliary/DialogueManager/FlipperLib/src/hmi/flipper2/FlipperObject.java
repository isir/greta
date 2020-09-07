package hmi.flipper2;

import java.util.Set;

import hmi.flipper2.dataflow.DataFlow;

public abstract class FlipperObject implements DataFlow {
	
	private String id;
	public  Set<String> dfin = null;
	public  Set<String> dfout = null;
	
	private static int idCount = 100;
	
	public FlipperObject(String id) {
		this.id = (id==null) ? "fid"+idCount++ : id;
	}
	
	public String id() {
		return this.id;
	}
	
	public void addPrefix(String pfx) {
		this.id = pfx + ":" + id();
	}
	
}
