package de.npecomplete.mc.testproject.lisp.special;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.util.LispElf;

/**
 * Takes a single form as an argument. Returns that argument unevaluated.
 */
final class QuoteSpecialForm implements SpecialForm {

	@Override
	public Object apply(Sequence args, Environment env) {
		if (!LispElf.matchSize(args, 1, 1)) {
			throw new LispException("'quote' requires exactly 1 argument: (quote FORM)");
		}
		return args.first();
	}
}
