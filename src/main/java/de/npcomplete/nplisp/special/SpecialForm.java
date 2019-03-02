package de.npcomplete.nplisp.special;

import java.util.List;

import de.npcomplete.nplisp.Environment;
import de.npcomplete.nplisp.data.Sequence;

public interface SpecialForm {

	Object apply(Sequence args, Environment env, boolean allowRecur);

	// BASE IMPLEMENTATIONS //

	/**
	 * Takes two arguments, a symbol and a form:
	 * <code>(def SYMBOL FORM)</code><br>
	 * The form is evaluated and the resulting value
	 * bound to the symbol in the global environment
	 * and then returned.
	 */
	SpecialForm DEF = new DefSpecialForm();

	/**
	 * Takes any number of arguments. Evaluates the
	 * arguments and returns the result of the last
	 * evaluation.
	 */
	SpecialForm DO = new DoSpecialForm();

	/**
	 * Takes a parameter vector and one or more forms as arguments.
	 * Returns a function that when called, will bind the passed parameters
	 * to the symbols specified by the parameter vector and evaluate
	 * the body forms as if they are in an implicit 'do' block.
	 */
	SpecialForm FN = new FnSpecialForm();

	/**
	 * Takes 2-3 arguments: TEST, THEN, [ELSE].
	 * Evaluates TEST first. If TEST results in a truthy
	 * value, THEN is evaluated, otherwise ELSE is evaluated.
	 */
	SpecialForm IF = new IfSpecialForm();

	/**
	 * Takes a binding form (a {@link List}) with symbol-value
	 * pairs, and a body (one or more forms).
	 * Binds the values to the symbols for code executed
	 * within the body.
	 */
	SpecialForm LET = new LetSpecialForm();

	/**
	 * Takes a single argument. Returns that argument unevaluated.
	 */
	SpecialForm QUOTE = new QuoteSpecialForm();
}
