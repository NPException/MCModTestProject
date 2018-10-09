package de.npecomplete.mc.testproject.lisp;

import static clojure.lang.Compiler.eval;

import de.npecomplete.mc.testproject.lisp.data.LispSequence;

public interface LispSpecialForm {
	Object apply(LispSequence args, LispEnvironment env);

	/**
	 * Takes 2-3 arguments: TEST, THEN, [ELSE].
	 * Evaluates TEST first. If TEST results in a truthy
	 * value, THEN is evaluated, otherwise ELSE is evaluated.
	 */
	LispSpecialForm IF = (args, env) -> {
		Object test = eval(args.first());
		return LispElf.truthy(test)
				? eval(args.more().first())
				: eval(args.more().more().first());
	};

	LispSpecialForm DO = (args, env) -> {
		return null; // TODO
	};

	LispSpecialForm QUOTE = (args, env) -> {
		return null; // TODO
	};

	LispSpecialForm DEF = (args, env) -> {
		return null; // TODO
	};

	LispSpecialForm FN = (args, env) -> {
		return null; // TODO
	};
}
