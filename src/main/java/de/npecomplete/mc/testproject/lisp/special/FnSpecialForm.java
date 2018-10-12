package de.npecomplete.mc.testproject.lisp.special;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.Lisp;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.function.LispFunction;
import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

final class FnSpecialForm implements SpecialForm {
	@Override
	public Object apply(Sequence args, Environment env) {
		if (args.empty()) {
			throw new LispException("'fn' requires at least one argument: (fn ARGS *&BODY*)");
		}
		Object arg1 = args.first();
		if (!(arg1 instanceof List)) {
			throw new LispException("'fn' first argument is not a List");
		}
		List<?> fnArgs = (List) arg1;

		// verify function argument list
		for (Object sym : fnArgs) {
			if (!(sym instanceof Symbol)) {
				String s = LispPrinter.printStr(sym);
				throw new LispException("'fn' argument is not a symbol: " + s);
			}
		}

		Sequence bodyForms = args.next();
		Function<Environment, Object> functionBody = localEnv -> {
			Object result = null;
			Sequence body = bodyForms;
			while (body != null && !body.empty()) {
				result = Lisp.eval(body.first(), localEnv);
				body = body.next();
			}
			return result;
		};

		switch (fnArgs.size()) {
			case 0:
				return new LispFunction() {
					@Override
					public Object apply() {
						return functionBody.apply(env);
					}
				};
			case 1:
				Symbol parSym1 = (Symbol) fnArgs.get(0);
				return new LispFunction() {
					@Override
					public Object apply(Object par) {
						Environment localEnv = new Environment(env);
						localEnv.bind(parSym1, par);
						return functionBody.apply(localEnv);
					}
				};
			case 2:
				parSym1 = (Symbol) fnArgs.get(0);
				Symbol parSym2 = (Symbol) fnArgs.get(1);
				return new LispFunction() {
					@Override
					public Object apply(Object par1, Object par2) {
						Environment localEnv = new Environment(env);
						localEnv.bind(parSym1, par1);
						localEnv.bind(parSym2, par2);
						return functionBody.apply(localEnv);
					}
				};
			case 3:
				parSym1 = (Symbol) fnArgs.get(0);
				parSym2 = (Symbol) fnArgs.get(1);
				Symbol parSym3 = (Symbol) fnArgs.get(2);
				return new LispFunction() {
					@Override
					public Object apply(Object par1, Object par2, Object par3) {
						Environment localEnv = new Environment(env);
						localEnv.bind(parSym1, par1);
						localEnv.bind(parSym2, par2);
						localEnv.bind(parSym3, par3);
						return functionBody.apply(localEnv);
					}
				};
			default:
				Iterator<?> it = fnArgs.iterator();
				parSym1 = (Symbol) it.next();
				parSym2 = (Symbol) it.next();
				parSym3 = (Symbol) it.next();

				Symbol[] moreSym = new Symbol[fnArgs.size() - 3];
				int i=0;
				do {
					moreSym[i++] = (Symbol) it.next();
				} while (it.hasNext());

				return new LispFunction() {
					@Override
					public Object apply(Object par1, Object par2, Object par3, Object... more) {
						if (more.length != moreSym.length) {
							throw new LispException("Wrong arity: " + (3 + more.length));
						}
						Environment localEnv = new Environment(env);
						localEnv.bind(parSym1, par1);
						localEnv.bind(parSym2, par2);
						localEnv.bind(parSym3, par3);

						for (int i=0, size=more.length; i<size; i++) {
							localEnv.bind(moreSym[i], more[i]);
						}

						return functionBody.apply(localEnv);
					}
				};
		}
	}
}
