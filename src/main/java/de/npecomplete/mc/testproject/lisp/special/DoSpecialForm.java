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
	public Object apply(Sequence args, Environment env) {
		Object result = null;
		while (args != null && !args.empty()) {
			result = Lisp.eval(args.first(), env);
			args = args.next();
		}
		return result;
	}
}
