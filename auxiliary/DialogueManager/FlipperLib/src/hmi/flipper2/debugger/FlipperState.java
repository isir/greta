package hmi.flipper2.debugger;

import hmi.flipper2.debugger.FlipperDebugger.Action;

class FlipperState {

	FlipperState prev = null, next = null;
	public Action action;
	public String id;
	public long startTime, stopTime;
	public String startArg, stopArg;

	public FlipperState push() {
		return this.next;
	}

	public FlipperState pop() {
		return this.prev;
	}

	public static FlipperState create(int size) {
		FlipperState p = null;
		for (int i = 0; i < size; i++) {
			FlipperState n = new FlipperState();
			n.next = p;
			if (p != null)
				p.prev = n;
			p = n;
		}
		return p;
	}

	public long duration() {
		return stopTime - startTime;
	}
}
