package de.npecomplete.mc.testproject.lisp.special;

import java.util.List;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.function.MultiArityFunction;
import de.npecomplete.mc.testproject.lisp.function.SingleArityFunction;

final class FnSpecialForm implements SpecialForm {
	@Override
	public Object apply(Sequence args, Environment env) {
		if (args.empty()) {
			throw new LispException("'fn' requires at least one argument: (fn [ARGS] *&BODY*)" +
					" or for multiple arities: (fn ([] *&BODY*) ([x] *&BODY*))");
		}

		if (args.first() instanceof List) {
			return SingleArityFunction.create(args, env);
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
}
