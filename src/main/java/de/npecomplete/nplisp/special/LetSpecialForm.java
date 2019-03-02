package de.npecomplete.nplisp.special;

import java.util.Iterator;
import java.util.List;

import de.npecomplete.nplisp.Environment;
import de.npecomplete.nplisp.Lisp;
import de.npecomplete.nplisp.LispException;
import de.npecomplete.nplisp.data.Sequence;
import de.npecomplete.nplisp.data.Symbol;
import de.npecomplete.nplisp.util.LispPrinter;

/**
 * Takes a binding form (a {@link List}) with symbol-value
 * pairs, and a body (one or more forms).
 * Binds the values to the symbols for code executed
 * within the body.
 */
final class LetSpecialForm implements SpecialForm {

	@Override
	public Object apply(Sequence args, Environment env, boolean allowRecur) {
		if (args.empty()) {
			throw new LispException("'let' requires at least one argument: (let BINDINGS *&FORMS*)");
		}
		Object arg1 = args.first();
		if (!(arg1 instanceof List)) {
			throw new LispException("'let' first argument is not a List");
		}
		List<?> bindings = (List) arg1;
		if (bindings.size() % 2 != 0) {
			throw new LispException("'let' bindings List doesn't have an even number of elements");
		}

		Environment localEnv = new Environment(env);

		// process bindings
		Iterator<?> it = bindings.iterator();
		while (it.hasNext()) {
			Object sym = it.next();
			if (!(sym instanceof Symbol)) {
				String s = LispPrinter.prStr(sym);
				throw new LispException("'let' binding target is not a symbol: " + s);
			}
			Object value = Lisp.eval(it.next(), localEnv, false);
			localEnv.bind((Symbol) sym, value);
		}

		// evaluate body
		return SpecialForm.DO.apply(args.next(), localEnv, allowRecur);
	}
}
