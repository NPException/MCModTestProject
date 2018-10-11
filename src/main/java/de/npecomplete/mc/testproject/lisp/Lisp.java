package de.npecomplete.mc.testproject.lisp;

import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.function.LispFunction;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;

public class Lisp {
	public final Environment globalEnv;

	public Lisp() {
		globalEnv = new Environment(null);
	}

	public void initStandardEnvironment() {
		globalEnv.bind("if", SpecialForm.IF);
		globalEnv.bind("def", SpecialForm.DEF);
		globalEnv.bind("let", SpecialForm.LET);
		globalEnv.bind("fn", SpecialForm.FN);
		globalEnv.bind("do", SpecialForm.DO);
		globalEnv.bind("quote", SpecialForm.QUOTE);
	}

	public Object eval(Object obj) throws LispException {
		return eval(obj, globalEnv);
	}

	public static Object eval(Object obj, Environment env) throws LispException {
		if (obj instanceof Symbol) {
			return env.lookup(((Symbol) obj).name);
		}

		if (obj instanceof Sequence) {
			Sequence seq = (Sequence) obj;
			if (seq.empty()) {
				throw new LispException("Can't eval empty sequence");
			}
			// evaluate first element
			Object first = eval(seq.first(), env);

			// call to special form
			if (first instanceof SpecialForm) {
				Sequence args = seq.more();
				return ((SpecialForm) first).apply(args, env);
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
