package de.npecomplete.mc.testproject.lisp;

import java.util.Arrays;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.data.ListSequence;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;
import de.npecomplete.mc.testproject.lisp.util.LispElf;
import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

public class Playground {
	private static Sequence Seq(Object... value) {
		return new ListSequence(Arrays.asList(value), 0);
	}

	private static List<Object> List(Object... value) {
		return Arrays.asList(value);
	}

	private static final SpecialForm printlnForm = (args, env) -> {
		System.out.print("> ");
		while (args != null && !args.empty()) {
			System.out.print(Lisp.eval(args.first(), env));
			args = args.next();
		}
		System.out.println();
		return null;
	};

	private static final SpecialForm prnStrForm = (args, env) -> {
		if (!LispElf.matchSize(args, 1, 1)) {
			throw new LispException("'prn-str' function requires exactly 1 argument: (prn-str ARG)");
		}
		return LispPrinter.printStr(Lisp.eval(args.first(), env));
	};

	public static void main(String[] arguments) throws Exception {
		Lisp lisp = new Lisp();
		lisp.initStandardEnvironment();

		Symbol DEF = new Symbol("def");

		Symbol PRINTLN = new Symbol("println");
		lisp.eval(Seq(DEF, PRINTLN, printlnForm));

		Symbol PRN_STR = new Symbol("prn-str");
		lisp.eval(Seq(DEF, PRN_STR, prnStrForm));

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

		run(lisp, form);

		run(lisp, Seq(new Symbol("is-dead")));
	}

	private static void run(Lisp lisp, Object form) {
		System.out.println();
		System.out.println("Evaluating:");
		System.out.println(LispPrinter.printStr(form));
		System.out.println("----------");
		try {
			Object result = lisp.eval(form);
			System.out.println("----------");
			System.out.println("Result: " + LispPrinter.printStr(result));
		} catch (Exception e) {
			System.out.println("----------");
			System.out.println("Failed(" + e.getClass().getSimpleName() + "): " + e.getMessage());
		}
	}
}
