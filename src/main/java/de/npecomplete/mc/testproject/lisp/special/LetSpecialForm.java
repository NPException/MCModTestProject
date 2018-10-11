package de.npecomplete.mc.testproject.lisp.special;

import java.util.Iterator;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.Lisp;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

/**
 * Takes a binding form (a {@link List}) with symbol-value
 * pairs, and a body (one or more forms).
 * Binds the values to the symbols for code executed
 * within the body.
 */
final class LetSpecialForm implements SpecialForm {

	@Override
	public Object apply(Sequence args, Environment env) {
		if (args.empty()) {
			throw new LispException("'let' requires at least one argument");
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
				String s = LispPrinter.printStr(sym);
				throw new LispException("'let' binding target is not a symbol: " + s);
			}
			Object value = Lisp.eval(it.next(), localEnv);
			localEnv.bind(((Symbol) sym).name, value);
		}

		// evaluate body
		Sequence body = args.next();

		Object result = null;
		while (body != null && !body.empty()) {
			result = Lisp.eval(body.first(), localEnv);
			body = body.next();
		}
		return result;
	}
}
