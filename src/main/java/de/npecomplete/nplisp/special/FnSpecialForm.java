package de.npecomplete.nplisp.special;

import java.util.List;

import de.npecomplete.nplisp.Environment;
import de.npecomplete.nplisp.LispException;
import de.npecomplete.nplisp.data.Sequence;
import de.npecomplete.nplisp.data.Symbol;
import de.npecomplete.nplisp.function.LispFunction;
import de.npecomplete.nplisp.function.MultiArityFunction;
import de.npecomplete.nplisp.function.MultiArityFunction.Builder;
import de.npecomplete.nplisp.function.SingleArityFunction;

/**
 * Takes a parameter vector and one or more forms as arguments.
 * Returns a function that when called, will bind the passed parameters
 * to the symbols specified by the parameter vector and evaluate
 * the body forms as if they are in an implicit 'do' block.
 */
final class FnSpecialForm implements SpecialForm {

	@Override
	public LispFunction apply(Sequence args, Environment env, boolean allowRecur) {
		Symbol name = null;

		if (args.first() instanceof Symbol) {
			name = (Symbol) args.first();
			args = args.more();
		}

		if (args.empty()) {
			throw new LispException("'fn' requires at least one argument: (fn name? [params*] exprs*)" +
					" or for multiple arities: (fn name? ([params*] exprs*) +)");
		}

		Object arg1 = args.first();
		if (arg1 instanceof List) {
			List fnArgs = (List) arg1;
			Sequence body = args.next();
			return new SingleArityFunction(name, env, body, fnArgs);
		}

		if (!(arg1 instanceof Sequence)) {
			throw new LispException("'fn' first argument must either be a list or a vector");
		}

		Builder fnBuilder = new MultiArityFunction.Builder(name, env);
		for (Object arg : args) {
			if (!(arg instanceof Sequence)) {
				throw new LispException("Arity variant of 'fn' must be a list");
			}
			fnBuilder.addArity((Sequence) arg);
		}
		return fnBuilder.build();
	}

}