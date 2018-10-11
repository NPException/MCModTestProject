package de.npecomplete.mc.testproject.lisp.special;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.Lisp;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.util.LispElf;

/**
 * Takes 2-3 arguments: TEST, THEN, [ELSE].
 * Evaluates TEST first. If TEST results in a truthy
 * value, THEN is evaluated, otherwise ELSE is evaluated.
 */
final class IfSpecialForm implements SpecialForm {

	@Override
	public Object apply(Sequence args, Environment env) {
		if (!LispElf.matchSize(args, 2, 3)) {
			throw new LispException("'if' special form requires 2 or 3 arguments: (if TEST THEN *ELSE*)");
		}
		Object test = Lisp.eval(args.first(), env);
		return LispElf.truthy(test)
				? Lisp.eval(args.more().first(), env)
				: Lisp.eval(args.more().more().first(), env);
	}
}
