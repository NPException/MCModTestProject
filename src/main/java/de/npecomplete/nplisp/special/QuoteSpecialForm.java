package de.npecomplete.nplisp.special;

import de.npecomplete.nplisp.Environment;
import de.npecomplete.nplisp.LispException;
import de.npecomplete.nplisp.data.Sequence;
import de.npecomplete.nplisp.util.LispElf;

/**
 * Takes a single form as an argument. Returns that argument unevaluated.
 */
final class QuoteSpecialForm implements SpecialForm {

	@Override
	public Object apply(Sequence args, Environment env, boolean allowRecur) {
		if (!LispElf.matchSize(args, 1, 1)) {
			throw new LispException("'quote' requires exactly 1 argument: (quote FORM)");
		}
		return args.first();
	}
}
