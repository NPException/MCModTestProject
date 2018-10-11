package de.npecomplete.mc.testproject.lisp.special;

import java.util.List;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.data.Sequence;

public interface SpecialForm {

	Object apply(Sequence args, Environment env);

	// BASE IMPLEMENTATIONS

	/**
	 * Takes 2-3 arguments: TEST, THEN, [ELSE].
	 * Evaluates TEST first. If TEST results in a truthy
	 * value, THEN is evaluated, otherwise ELSE is evaluated.
	 */
	SpecialForm IF = new IfSpecialForm();

	// TODO
	SpecialForm DEF = new DefSpecialForm();

	/**
	 * Takes a binding form (a {@link List}) with symbol-value
	 * pairs, and a body (one or more forms).
	 * Binds the values to the symbols for code executed
	 * within the body.
	 */
	SpecialForm LET = new LetSpecialForm();

	// TODO
	SpecialForm FN = new FnSpecialForm();

	/**
	 * Takes any number of arguments. Evaluates the
	 * arguments and returns the result of the last
	 * evaluation.
	 */
	SpecialForm DO = new DoSpecialForm();

	/**
	 * Takes a single argument. Returns that argument unevaluated.
	 */
	SpecialForm QUOTE = new QuoteSpecialForm();
}
