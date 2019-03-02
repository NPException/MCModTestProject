package de.npecomplete.nplisp.special;

import de.npecomplete.nplisp.Environment;
import de.npecomplete.nplisp.Lisp;
import de.npecomplete.nplisp.data.Sequence;

/**
 * Takes any number of arguments. Evaluates the
 * arguments and returns the result of the last
 * evaluation.
 */
final class DoSpecialForm implements SpecialForm {

	@Override
	public Object apply(Sequence body, Environment env, boolean allowRecur) {
		Object result = null;
		while (body != null && !body.empty()) {
			Object expr = body.first();
			body = body.next();
			boolean allowTailCall = allowRecur && body == null;
			result = Lisp.eval(expr, env, allowTailCall);
		}
		return result;
	}
}
