package de.npcomplete.nplisp.corelibrary;

import static de.npcomplete.nplisp.corelibrary.CoreLibrary.first;
import static de.npcomplete.nplisp.corelibrary.CoreLibrary.isSeq;

import de.npcomplete.nplisp.core.Namespace;

public final class Macroexpand {
	private Macroexpand() {
	}

	/**
	 * If form represents a macro form, returns its expansion,
	 * else returns form.
	 */
	public static Object macroexpand1(Object nsObj, Object form) {
		Namespace ns = (Namespace) nsObj;
		if (isSeq(form)) {
			Object op = first(form);
			// TODO
		}
		return form;
	}

	/**
	 * Repeatedly calls macroexpand-1 on form until it no longer
	 * represents a macro form, then returns it.  Note neither
	 * macroexpand1 nor macroexpand expand macros in subforms.
	 */
	public static Object macroexpand(Object nsObj, Object form) {
		Namespace ns = (Namespace) nsObj;
		Object ex = macroexpand1(ns, form);
		while (ex != form) {
			form = ex;
			ex = macroexpand1(ns, form);
		}
		return ex;
	}

	/**
	 * Recursively performs all possible macroexpansions in form.
	 */
	public static Object macroexpandAll(Object nsObj, Object form) {
		Namespace ns = (Namespace) nsObj;
		// TODO
		return form;
	}
}
