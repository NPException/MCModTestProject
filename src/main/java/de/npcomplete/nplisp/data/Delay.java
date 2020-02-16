package de.npcomplete.nplisp.data;

import de.npcomplete.nplisp.function.LispFunctionFactory.Fn0;
import de.npcomplete.nplisp.util.Util;

public class Delay {
	private Object val;
	private Throwable exception;
	private Fn0 fn;

	public Delay(Fn0 fn) {
		this.fn = fn;
		this.val = null;
		this.exception = null;
	}

	public Object deref() {
		if (fn != null) {
			try {
				val = fn.apply();
			} catch (Throwable t) {
				exception = t;
			}
			fn = null;
		}
		if (exception != null) {
			throw Util.sneakyThrow(exception);
		}
		return val;
	}
}
