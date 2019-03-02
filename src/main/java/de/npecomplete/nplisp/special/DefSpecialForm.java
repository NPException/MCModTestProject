package de.npecomplete.nplisp.special;

import de.npecomplete.nplisp.Environment;
import de.npecomplete.nplisp.Lisp;
import de.npecomplete.nplisp.LispException;
import de.npecomplete.nplisp.data.Sequence;
import de.npecomplete.nplisp.data.Symbol;
import de.npecomplete.nplisp.util.LispElf;
import de.npecomplete.nplisp.util.LispPrinter;

/**
 * Takes two arguments, a symbol and a form:
 * <code>(def SYMBOL FORM)</code><br>
 * The form is evaluated and the resulting value
 * bound to the symbol in the global environment
 * and then returned.
 */
final class DefSpecialForm implements SpecialForm {

	@Override
	public Object apply(Sequence args, Environment env, boolean allowRecur) {
		if (!LispElf.matchSize(args, 2, 2)) {
			throw new LispException("'def' requires 2 arguments: (def SYMBOL FORM)");
		}
		Object sym = args.first();
		if (!(sym instanceof Symbol)) {
			String s = LispPrinter.prStr(sym);
			throw new LispException("'def' binding target is not a symbol: " + s);
		}
		Object form = args.next().first();
		Object value = Lisp.eval(form, env, false);
		env.top().bind((Symbol) sym, value);
		return value;
	}
}
