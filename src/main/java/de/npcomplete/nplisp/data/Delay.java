package de.npcomplete.nplisp.data;

import static de.npcomplete.nplisp.util.Util.sneakyThrow;

import de.npcomplete.nplisp.function.LispFunction;

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
			throw sneakyThrow(exception);
		}
		return val;
	}

	public static Object force(Object x) {
		return x instanceof Delay
				? ((Delay) x).deref()
				: x;
	}
}
