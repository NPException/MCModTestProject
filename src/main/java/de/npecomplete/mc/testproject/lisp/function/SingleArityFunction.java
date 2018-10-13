package de.npecomplete.mc.testproject.lisp.function;

import java.util.Iterator;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

public abstract class SingleArityFunction implements LispFunction {

	private SingleArityFunction() {
	}

	public static SingleArityFunction create(Sequence args, Environment env) {
		if (args.empty()) {
			throw new LispException("'fn' requires at least one argument: (fn [ARGS] *&BODY*)");
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

		Sequence body = args.next();

		switch (fnArgs.size()) {
			case 0:
				return new SingleArityFunction() {
					@Override
					public Object apply() {
						return MultiArityFunction.executeBody(body, env);
					}
				};
			case 1:
				Symbol parSym1 = (Symbol) fnArgs.get(0);
				return new SingleArityFunction() {
					@Override
					public Object apply(Object par) {
						Environment localEnv = new Environment(env);
						localEnv.bind(parSym1, par);
						return MultiArityFunction.executeBody(body, localEnv);
					}
				};
			case 2:
				parSym1 = (Symbol) fnArgs.get(0);
				Symbol parSym2 = (Symbol) fnArgs.get(1);
				return new SingleArityFunction() {
					@Override
					public Object apply(Object par1, Object par2) {
						Environment localEnv = new Environment(env);
						localEnv.bind(parSym1, par1);
						localEnv.bind(parSym2, par2);
						return MultiArityFunction.executeBody(body, localEnv);
					}
				};
			case 3:
				parSym1 = (Symbol) fnArgs.get(0);
				parSym2 = (Symbol) fnArgs.get(1);
				Symbol parSym3 = (Symbol) fnArgs.get(2);
				return new SingleArityFunction() {
					@Override
					public Object apply(Object par1, Object par2, Object par3) {
						Environment localEnv = new Environment(env);
						localEnv.bind(parSym1, par1);
						localEnv.bind(parSym2, par2);
						localEnv.bind(parSym3, par3);
						return MultiArityFunction.executeBody(body, localEnv);
					}
				};
			default:
				Iterator<?> it = fnArgs.iterator();
				parSym1 = (Symbol) it.next();
				parSym2 = (Symbol) it.next();
				parSym3 = (Symbol) it.next();
				Symbol parSym4 = (Symbol) it.next();

				Symbol[] moreSym = new Symbol[fnArgs.size() - 4];
				int i = 0;
				while (it.hasNext()) {
					moreSym[i++] = (Symbol) it.next();
				}

				return new SingleArityFunction() {
					@Override
					public Object apply(Object par1, Object par2, Object par3, Object par4, Object... more) {
						if (more.length != moreSym.length) {
							throw new LispException("Wrong arity: " + (4 + more.length));
						}
						Environment localEnv = new Environment(env);
						localEnv.bind(parSym1, par1);
						localEnv.bind(parSym2, par2);
						localEnv.bind(parSym3, par3);
						localEnv.bind(parSym4, par4);

						int moreSize = more.length;
						if (moreSize > 0) {
							for (int i = 0; i < moreSize; i++) {
								localEnv.bind(moreSym[i], more[i]);
							}
						}

						return MultiArityFunction.executeBody(body, localEnv);
					}
				};
		}
	}
}
