package de.npcomplete.nplisp.special;

import de.npcomplete.nplisp.Environment;
import de.npcomplete.nplisp.Lisp;
import de.npcomplete.nplisp.data.Sequence;

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
