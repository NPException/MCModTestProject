package de.npcomplete.nplisp.special;

import de.npcomplete.nplisp.Environment;
import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.data.Symbol;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.function.Macro;
import de.npcomplete.nplisp.util.LispElf;
import de.npcomplete.nplisp.util.LispPrinter;

/**
 * Takes at least 2 arguments (a symbol and an argument vector),
 * and and ideally one or more body forms.<br>
 * <code>(defmacro name [params*] body*)</code><br>
 * <code>(defmacro name ([params*] body*) +)</code><br>
 * Returns a macro which will transform the provided arguments
 * as specified by the body.
 */
final class DefmacroSpecialForm implements SpecialForm {
	@Override
	public Macro apply(Sequence args, Environment env, boolean allowRecur) {
		if (!LispElf.minSize(args, 2)) {
			throw new LispException("'defmacro' requires at least 2 arguments: (defmacro name [params*] body*)");
		}
		Object sym = args.first();
		if (!(sym instanceof Symbol)) {
			String s = LispPrinter.prStr(sym);
			throw new LispException("'defmacro' binding target is not a symbol: " + s);
		}
		LispFunction macroFunction = (LispFunction) SpecialForm.FN.apply(args, env, false /*not used in FN*/);
		Macro macro = macroFunction::applyTo;
		env.top().bind((Symbol) sym, macro);
		return null;
	}
}
