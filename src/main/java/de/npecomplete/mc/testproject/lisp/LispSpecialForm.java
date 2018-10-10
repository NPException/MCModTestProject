package de.npecomplete.mc.testproject.lisp;

import static de.npecomplete.mc.testproject.lisp.Lisp.eval;

import de.npecomplete.mc.testproject.lisp.data.LispSequence;
import de.npecomplete.mc.testproject.lisp.util.LispElf;

public interface LispSpecialForm {
	Object apply(LispSequence args, LispEnvironment env);

	/**
	 * Takes 2-3 arguments: TEST, THEN, [ELSE].
	 * Evaluates TEST first. If TEST results in a truthy
	 * value, THEN is evaluated, otherwise ELSE is evaluated.
	 */
	LispSpecialForm IF = (args, env) -> {
		if (!LispElf.matchSize(args, 2, 3)) {
			throw new LispException("'if' special form requires 2 or 3 arguments: (if TEST THEN *ELSE*)");
		}
		Object test = eval(args.first(), env);
		return LispElf.truthy(test)
				? eval(args.more().first(), env)
				: eval(args.more().more().first(), env);
	};

	LispSpecialForm DEF = (args, env) -> {
		return null; // TODO
	};

	LispSpecialForm LET = (args, env) -> {
		return null; // TODO
	};

	LispSpecialForm FN = (args, env) -> {
		return null; // TODO
	};

	/**
	 * Takes any number of arguments. Evaluates the
	 * arguments and returns the result of the last
	 * evaluation.
	 */
	LispSpecialForm DO = (args, env) -> {
		Object result = null;
		while (args != null && !args.empty()) {
			result = eval(args.first(), env);
			args = args.next();
		}
		return result;
	};

	/**
	 * Takes a single argument. Returns that argument unevaluated.
	 */
	LispSpecialForm QUOTE = (args, env) -> {
		if (!LispElf.matchSize(args, 1, 1)) {
			throw new LispException("'quote' special form requires exactly 1 argument: (quote FORM)");
		}
		return args.first();
	};
}
