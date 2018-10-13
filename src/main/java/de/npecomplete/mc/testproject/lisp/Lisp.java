package de.npecomplete.mc.testproject.lisp;

import java.util.ArrayList;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.function.LispFunction;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;
import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

public class Lisp {
	public final Environment globalEnv;

	public Lisp() {
		globalEnv = new Environment(null);
	}

	public void initStandardEnvironment() {
		globalEnv.bind(new Symbol("def"), SpecialForm.DEF);
		globalEnv.bind(new Symbol("do"), SpecialForm.DO);
		globalEnv.bind(new Symbol("fn"), SpecialForm.FN);
		globalEnv.bind(new Symbol("if"), SpecialForm.IF);
		globalEnv.bind(new Symbol("let"), SpecialForm.LET);
		globalEnv.bind(new Symbol("quote"), SpecialForm.QUOTE);
	}

	public Object eval(Object obj) throws LispException {
		return eval(obj, globalEnv);
	}

	public static Object eval(Object obj, Environment env) throws LispException {
		if (obj instanceof Symbol) {
			return env.lookup((Symbol) obj);
		}

		if (obj instanceof Sequence) {
			Sequence seq = (Sequence) obj;
			if (seq.empty()) {
				throw new LispException("Can't eval empty sequence");
			}
			// evaluate first element
			Object callable = eval(seq.first(), env);

			// call to special form
			if (callable instanceof SpecialForm) {
				Sequence args = seq.more();
				return ((SpecialForm) callable).apply(args, env);
			}

			// call to function
			LispFunction fn = LispFunction.from(callable);
			if (fn != null) {
				Sequence args = seq.more();
				if (args.empty()) {
					return fn.apply(); // no arguments
				}

				Object arg1 = eval(args.first(), env);
				args = args.next();
				if (args == null) {
					return fn.apply(arg1); // one argument
				}

				Object arg2 = eval(args.first(), env);
				args = args.next();
				if (args == null) {
					return fn.apply(arg1, arg2); // two arguments
				}

				Object arg3 = eval(args.first(), env);
				args = args.next();
				if (args == null) {
					return fn.apply(arg1, arg2, arg3); // three arguments
				}

				Object arg4 = eval(args.first(), env);
				args = args.next();
				if (args == null) {
					return fn.apply(arg1, arg2, arg3, arg4); // four arguments
				}

				// more than four arguments
				List<Object> moreArgs = new ArrayList<>(3);
				do {
					moreArgs.add(eval(args.first(), env));
				} while ((args = args.next()) != null);
				return fn.apply(arg1, arg2, arg3, arg4, moreArgs.toArray());
			}

			String call = LispPrinter.printStr(seq);
			String first = LispPrinter.printStr(seq.first());
			throw new LispException("Can't call " + callable + " | "
					+ "Was returned when evaluating: " + first + " | "
					+ "Call: " + call);
		}

		return obj;
	}
}
