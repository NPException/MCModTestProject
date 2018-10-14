package de.npecomplete.mc.testproject.lisp.special;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.Lisp;
import de.npecomplete.mc.testproject.lisp.data.Sequence;

/**
 * Takes any number of arguments. Evaluates the
 * arguments and returns the result of the last
 * evaluation.
 */
final class DoSpecialForm implements SpecialForm {

	@Override
	public Object apply(Sequence body, Environment env) {
		Object result = null;
		while (body != null && !body.empty()) {
			result = Lisp.eval(body.first(), env);
			body = body.next();
		}
		return result;
	}
}
