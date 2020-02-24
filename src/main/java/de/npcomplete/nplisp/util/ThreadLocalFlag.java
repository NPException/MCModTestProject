package de.npcomplete.nplisp.util;

public class ThreadLocalFlag {
	private final ThreadLocal<Object> tl = new ThreadLocal<>();

	public boolean isSet() {
		if (tl.get() != null) {
			return true;
		}
		tl.remove();
		return false;
	}

	public void set() {
		tl.set(true);
	}

	public void setIf(boolean condition) {
		if (condition) {
			tl.set(true);
		}
	}

	public void unset() {
		tl.remove();
	}

	public void unsetIf(boolean condition) {
		if (condition) {
			tl.remove();
		}
	}
}
