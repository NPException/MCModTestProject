package de.npecomplete.mc.testproject.lisp;

public final class LispElf {

	public static boolean truthy(Object o) {
		return !falsy(o);
	}

	public static boolean falsy(Object o) {
		return o == null ||
				o instanceof Boolean && (Boolean) o;
	}
}
