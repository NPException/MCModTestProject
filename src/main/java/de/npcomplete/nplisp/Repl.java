package de.npcomplete.nplisp;

import de.npcomplete.nplisp.data.CoreLibrary;
import de.npcomplete.nplisp.data.Symbol;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.function.LispFunctionFactory.Fn1;
import de.npcomplete.nplisp.util.LispReader;

@SuppressWarnings("unchecked")
public class Repl {
	public static final Symbol IN_NS_SYMBOL = new Symbol("in-ns");

	private final Namespace[] currentReplNs = new Namespace[1];
	private final LispFunction in_ns_function;

	public Repl(Lisp lisp) {
		currentReplNs[0] = lisp.namespaces.getOrCreateNamespace("user");
		/*
		 * This function switches the current REPL execution environment to the desired namespace,
		 * creating it if it does not yet exist.
		 */
		in_ns_function = (Fn1) par -> {
			String name = CoreLibrary.str(par);
			Namespace ns = lisp.namespaces.getOrCreateNamespace(name);
			currentReplNs[0] = ns;
			return ns;
		};
	}

	public Namespace currentNs() {
		return currentReplNs[0];
	}

	/**
	 * Evaluates the given form in the current namespace.
	 * (note: generics are only used to safe the caller an explicit cast)
	 */
	public <T> T eval(Object form) {
		Namespace ns = currentNs();
		Environment replEnv = new Environment(ns);
		replEnv.bind(IN_NS_SYMBOL, in_ns_function);
		return (T) Lisp.eval(form, replEnv, false);
	}

	/**
	 * Reads the string and evaluates the resulting form in the current namespace.
	 * (note: generics are only used to safe the caller an explicit cast)
	 */
	public <T> T evalStr(String value) {
		return eval(LispReader.readStr(value));
	}
}
