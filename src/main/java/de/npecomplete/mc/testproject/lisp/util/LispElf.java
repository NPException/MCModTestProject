package de.npecomplete.mc.testproject.lisp.util;

import static de.npecomplete.mc.testproject.lisp.Lisp.eval;

import java.util.Arrays;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.Lisp;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.ListSequence;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;

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
	public static boolean matchSize(Sequence seq, int min, int max) {
		return minSize(seq, min) && maxSize(seq, max);
	}

	/**
	 * @return true if the given {@link Sequence} is not null
	 * and contains at least 'min' elements.
	 */
	public static boolean minSize(Sequence seq, int min) {
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
	 * @return true if the given {@link Sequence} is not null
	 * and contains at most 'max' elements.
	 */
	public static boolean maxSize(Sequence seq, int max) {
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

	// PLAYGROUND //

	public static Sequence Seq(Object... value) {
		return new ListSequence(Arrays.asList(value), 0);
	}

	public static List<Object> List(Object... value) {
		return Arrays.asList(value);
	}

	private static final SpecialForm printlnForm = (args, env) -> {
		while (args != null && !args.empty()) {
			System.out.print(eval(args.first(), env));
			args = args.next();
		}
		System.out.println();
		return null;
	};

	private static final SpecialForm prnStrForm = (args, env) -> {
		if (!LispElf.matchSize(args, 1, 1)) {
			throw new LispException("'prn-str' function requires exactly 1 argument: (prn-str ARG)");
		}
		return LispPrinter.printStr(eval(args.first(), env));
	};

	public static void main(String[] arguments) throws Exception {
		Lisp lisp = new Lisp();
		lisp.initStandardEnvironment();

		Symbol PRINTLN = new Symbol("println");
		lisp.globalEnv.bind(PRINTLN.name, printlnForm);

		Symbol PRN_STR = new Symbol("prn-str");
		lisp.globalEnv.bind(PRN_STR.name, prnStrForm);

		Symbol DO = new Symbol("do");
		Symbol IF = new Symbol("if");
		Symbol QUOTE = new Symbol("quote");
		Symbol LET = new Symbol("let");

		Symbol BLARG = new Symbol("blarg");

		// @formatter:off
		Object form =
		  Seq(DO,
		        Seq(IF, true,
		              Seq(PRINTLN, "Hello\nWorld!")),
		        Seq(IF, null,
		              Seq(PRINTLN, "you don't see me"),
		              Seq(PRINTLN, Seq(PRN_STR, Seq(QUOTE, Seq(PRINTLN, "I'm quoted!"))))),
		        Seq(LET, List(BLARG, PRINTLN,
		                      PRINTLN, QUOTE),
		              Seq(BLARG, Seq(PRINTLN, "\"let\" works!")),
		              Seq(BLARG, "It really works!"),
		              "The End."));
		// @formatter:off

		System.out.println("Evaluating:");
		System.out.println(LispPrinter.printStr(form));
		System.out.println("----------");
		System.out.println();

		Object result = lisp.eval(form);

		System.out.println();
		System.out.println("----------");
		System.out.println("Result: " + LispPrinter.printStr(result));
	}
}
