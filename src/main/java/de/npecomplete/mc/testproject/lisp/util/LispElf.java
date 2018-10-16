package de.npecomplete.mc.testproject.lisp.util;

import de.npecomplete.mc.testproject.lisp.data.Sequence;

public final class LispElf {

	/**
	 * @return true if the given object
	 * is not null and not false.
	 */
	public static boolean truthy(Object o) {
		return !falsy(o);
	}

	/**
	 * @return true if the given object
	 * is null or false.
	 */
	public static boolean falsy(Object o) {
		return o == null ||
				o instanceof Boolean && !(Boolean) o;
	}

	/**
	 * Checks if the given sequence has the specified
	 * number of elements.
	 */
	public static boolean matchSize(Sequence seq, int min, int max) {
		return minSize(seq, min) && maxSize(seq, max);
	}

	/**
	 * @return true if the given {@link Sequence} is not null
	 * and contains at least 'min' elements.
	 */
	public static boolean minSize(Sequence seq, int min) {
		if (seq == null || min > 0 && seq.empty()) {
			return false;
		}
		while (--min > 0) {
			seq = seq.next();
			if (seq == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true if the given {@link Sequence} is not null
	 * and contains at most 'max' elements.
	 */
	public static boolean maxSize(Sequence seq, int max) {
		if (seq == null || max <= 0 && !seq.empty()) {
			return false;
		}
		while ((seq = seq.next()) != null) {
			if (--max == 0) {
				return false;
			}
		}
		return true;
	}
}
