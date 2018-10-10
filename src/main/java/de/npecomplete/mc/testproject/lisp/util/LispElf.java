package de.npecomplete.mc.testproject.lisp.util;

import static de.npecomplete.mc.testproject.lisp.Lisp.eval;

import java.util.Arrays;

import de.npecomplete.mc.testproject.lisp.Lisp;
import de.npecomplete.mc.testproject.lisp.LispSpecialForm;
import de.npecomplete.mc.testproject.lisp.data.LispSequence;
import de.npecomplete.mc.testproject.lisp.data.ListSequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;

public final class LispElf {

	/**
	 * @return true if the given object
	 * is not null and not false.
	 */
	public static boolean truthy(Object o) {
		return !falsy(o);
	}

	/**
	 * @return true if the given object
	 * is null or false.
	 */
	public static boolean falsy(Object o) {
		return o == null ||
				o instanceof Boolean && !(Boolean) o;
	}

	/**
	 * Checks if the given sequence has the specified
	 * number of elements.
	 */
	public static boolean matchSize(LispSequence seq, int min, int max) {
		return minSize(seq, min) && maxSize(seq, max);
	}

	/**
	 * @return true if the given {@link LispSequence} is not null
	 * and contains at least 'min' elements.
	 */
	public static boolean minSize(LispSequence seq, int min) {
		if (seq == null || min > 0 && seq.empty()) {
			return false;
		}
		while (--min > 0) {
			seq = seq.next();
			if (seq == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true if the given {@link LispSequence} is not null
	 * and contains at most 'max' elements.
	 */
	public static boolean maxSize(LispSequence seq, int max) {
		if (seq == null || max <= 0 && !seq.empty()) {
			return false;
		}
		while ((seq = seq.next()) != null) {
			if (--max == 0) {
				return false;
			}
		}
		return true;
	}

	public static LispSequence Seq(Object... value) {
		return new ListSequence(Arrays.asList(value), 0);
	}

	// PLAYGROUND //

	private static final LispSpecialForm printForm = (args, env) -> {
		while (args != null && !args.empty()) {
			System.out.print(eval(args.first(), env));
			args = args.next();
		}
		System.out.println();
		return null;
	};

	public static void main(String[] arguments) {
		Lisp lisp = new Lisp();
		lisp.initStandardEnvironment();

		Symbol PRINT = new Symbol("print");
		lisp.globalEnv.bind(PRINT.name, printForm);

		Symbol DO = new Symbol("do");
		Symbol IF = new Symbol("if");
		Symbol QUOTE = new Symbol("quote");

		Object result = lisp.eval(
				Seq(DO,
						Seq(IF, true,
								Seq(PRINT, "Hello World")
						),
						Seq(IF, null,
								Seq(PRINT, Seq(QUOTE, DO)),
								Seq(PRINT, Seq(QUOTE, Seq(PRINT, "I'm quoted!")))
						)
				)
		);
		System.err.println("Result: " + result);
	}
}
