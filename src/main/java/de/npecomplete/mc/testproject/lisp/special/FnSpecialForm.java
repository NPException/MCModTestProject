package de.npecomplete.mc.testproject.lisp.special;

import java.util.Iterator;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.function.FourPlusArgFunction;
import de.npecomplete.mc.testproject.lisp.function.LispFunction;
import de.npecomplete.mc.testproject.lisp.function.MultiArityFunction;
import de.npecomplete.mc.testproject.lisp.function.OneArgFunction;
import de.npecomplete.mc.testproject.lisp.function.ThreeArgFunction;
import de.npecomplete.mc.testproject.lisp.function.TwoArgFunction;
import de.npecomplete.mc.testproject.lisp.function.ZeroArgFunction;
import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

final class FnSpecialForm implements SpecialForm {
	@Override
	public Object apply(Sequence args, Environment env) {
		if (args.empty()) {
			throw new LispException("'fn' requires at least one argument: (fn [ARGS] *&BODY*)" +
					" or for multiple arities: (fn ([] *&BODY*) ([x] *&BODY*))");
		}

		if (args.first() instanceof List) {
			return singleArityFunction(args, env);
		}

		if (!(args.first() instanceof Sequence)) {
			throw new LispException("'fn' first argument must either be a Sequence or a List");
		}

		MultiArityFunction function = new MultiArityFunction(env);
		for (Object arg : args) {
			if (!(arg instanceof Sequence)) {
				throw new LispException("Arity variant of 'fn' must be a Sequence");
			}
			function.addArity((Sequence) arg);
		}
		return function;
	}

	private static LispFunction singleArityFunction(Sequence args, Environment env) {
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

		if (fnArgs.isEmpty()) {
			return new ZeroArgFunction(env, body);
		}

		@SuppressWarnings("unchecked")
		Iterator<Symbol> symbols = (Iterator<Symbol>) fnArgs.iterator();

		Symbol parSym1 = symbols.next();
		if (!symbols.hasNext()) {
			return new OneArgFunction(env, body, parSym1);
		}

		Symbol parSym2 = symbols.next();
		if (!symbols.hasNext()) {
			return new TwoArgFunction(env, body, parSym1, parSym2);
		}

		Symbol parSym3 = symbols.next();
		if (!symbols.hasNext()) {
			return new ThreeArgFunction(env, body, parSym1, parSym2, parSym3);
		}

		Symbol parSym4 = symbols.next();
		Symbol[] moreSym = new Symbol[fnArgs.size() - 4];
		int i = 0;
		while (symbols.hasNext()) {
			moreSym[i++] = symbols.next();
		}
		return new FourPlusArgFunction(env, body, parSym1, parSym2, parSym3, parSym4, moreSym);
	}
}
