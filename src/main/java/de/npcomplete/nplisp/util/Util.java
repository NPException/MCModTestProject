package de.npcomplete.nplisp.util;

public final class Util {
	private Util() {
	}

	/**
	 * Throw even checked exceptions without being required
	 * to declare them or catch them. Suggested idiom:
	 * <p>
	 * <code>throw sneakyThrow( some exception );</code>
	 */
	public static RuntimeException sneakyThrow(Throwable t) {
		// http://www.mail-archive.com/javaposse@googlegroups.com/msg05984.html
		if (t == null) {
			throw new NullPointerException();
		}
		Util.sneakyThrow0(t);
		return new RuntimeException("How on earth did the execution reach this point?!");
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
		throw (T) t;
	}
}
