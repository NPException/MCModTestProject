package de.npcomplete.nplisp.data;

import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.util.Util;

public class Delay implements Deref {
	private Object val;
	private Throwable exception;
	private LispFunction fn;

	public Delay(LispFunction fn) {
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

	public static Object force(Object x) {
		return x instanceof Delay
				? ((Delay) x).deref()
				: x;
	}
}
