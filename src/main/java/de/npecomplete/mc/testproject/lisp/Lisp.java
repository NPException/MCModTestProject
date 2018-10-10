package de.npecomplete.mc.testproject.lisp;

import de.npecomplete.mc.testproject.lisp.data.LispSequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;

public class Lisp {
	public final LispEnvironment globalEnv;

	public Lisp() {
		globalEnv = new LispEnvironment(null);
	}

	public void initStandardEnvironment() {
		globalEnv.bind("if", LispSpecialForm.IF);
		globalEnv.bind("def", LispSpecialForm.DEF);
		globalEnv.bind("let", LispSpecialForm.LET);
		globalEnv.bind("fn", LispSpecialForm.FN);
		globalEnv.bind("do", LispSpecialForm.DO);
		globalEnv.bind("quote", LispSpecialForm.QUOTE);
	}

	public Object eval(Object obj) throws LispException {
		return eval(obj, globalEnv);
	}

	public static Object eval(Object obj, LispEnvironment env) throws LispException {
		if (obj instanceof Symbol) {
			return env.lookup(((Symbol) obj).name);
		}

		if (obj instanceof LispSequence) {
			LispSequence seq = (LispSequence) obj;
			if (seq.empty()) {
				throw new LispException("Can't eval empty sequence");
			}
			// evaluate first element
			Object first = eval(seq.first(), env);

			// call to special form
			if (first instanceof LispSpecialForm) {
				LispSequence args = seq.more();
				return ((LispSpecialForm) first).apply(args, env);
			}

			// TODO call to function
			LispFunction fn = LispFunction.from(first);
			if (fn != null) {
				// TODO call function with args
			}

			throw new LispException("Can't call " + first);
		}

		return obj;
	}
}
