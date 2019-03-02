package de.npecomplete.nplisp.special;

import de.npecomplete.nplisp.Environment;
import de.npecomplete.nplisp.Lisp;
import de.npecomplete.nplisp.LispException;
import de.npecomplete.nplisp.data.Sequence;
import de.npecomplete.nplisp.util.LispElf;

/**
 * Takes 2-3 arguments: TEST, THEN, [ELSE].
 * Evaluates TEST first. If TEST results in a truthy
 * value, THEN is evaluated, otherwise ELSE is evaluated.
 */
final class IfSpecialForm implements SpecialForm {

	@Override
	public Object apply(Sequence args, Environment env, boolean allowRecur) {
		if (!LispElf.matchSize(args, 2, 3)) {
			throw new LispException("'if' requires 2 or 3 arguments: (if TEST THEN *ELSE*)");
		}
		Object test = Lisp.eval(args.first(), env, false);
		return LispElf.truthy(test)
				? Lisp.eval(args.next().first(), env, allowRecur)
				: Lisp.eval(args.next().more().first(), env, allowRecur);
	}
}
