package de.npcomplete.nplisp.corelibrary;

import static de.npcomplete.nplisp.corelibrary.CoreLibrary.first;
import static de.npcomplete.nplisp.corelibrary.CoreLibrary.isSeq;
import static de.npcomplete.nplisp.corelibrary.CoreLibrary.isSymbol;

import java.util.function.BiFunction;

import de.npcomplete.nplisp.core.Namespace;
import de.npcomplete.nplisp.core.Var;
import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.data.Symbol;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.function.LispFunctionFactory.Fn2;
import de.npcomplete.nplisp.function.Macro;

public final class Macroexpand {
	private Macroexpand() {
	}

	/**
	 * If form represents a macro form, returns its expansion,
	 * else returns form.
	 */
	public static LispFunction macroexpand1(BiFunction<Var, Namespace, Object> derefVar) {
		return (Fn2) (nsObj, form) -> {
			Namespace ns = (Namespace) nsObj;
			if (isSeq(form)) {
				Object op = first(form);
				if (isSymbol(op)) {
					Var v = ns.lookupVar((Symbol) op, true, true);
					if (v != null && v.isMacro()) {
						Macro macro = (Macro) derefVar.apply(v, ns);
						form = macro.expand(((Sequence) form).more());
					}
				}
			}
			return form;
		};
	}

	/**
	 * Repeatedly calls macroexpand-1 on form until it no longer
	 * represents a macro form, then returns it.  Note neither
	 * macroexpand1 nor macroexpand expand macros in subforms.
	 */
	public static LispFunction macroexpand(LispFunction expand1) {
		return (Fn2) (nsObj, form) -> {
			Namespace ns = (Namespace) nsObj;
			Object ex = expand1.apply(ns, form);
			while (ex != form) {
				form = ex;
				ex = expand1.apply(ns, form);
			}
			return ex;
		};
	}

	/**
	 * Recursively performs all possible macroexpansions in form.
	 */
	public static LispFunction macroexpandAll(LispFunction expand) {
		return (Fn2) (nsObj, form) -> {
			Namespace ns = (Namespace) nsObj;
			// TODO
			return form;
		};
	}
}
