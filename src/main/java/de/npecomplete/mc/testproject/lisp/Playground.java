package de.npecomplete.mc.testproject.lisp;

import static de.npecomplete.mc.testproject.lisp.util.LispElf.Seq;

import java.util.Arrays;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;
import de.npecomplete.mc.testproject.lisp.util.LispElf;
import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

public class Playground {
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
		long start = System.nanoTime();
		try {
			start();
		} finally {
			long time = System.nanoTime() - start;
			System.out.println("Runtime: " + time + " ns");
		}
		Thread.sleep(100);
	}

	private static void start() {
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
		Symbol FN = new Symbol("fn");

		Symbol BLARG = new Symbol("blarg");

		Symbol FN_0 = new Symbol("fn-0");
		Symbol FN_1 = new Symbol("fn-1");
		Symbol ARG = new Symbol("arg");

		// @formatter:off
		Object form =
		  Seq(DO,
		        Seq(DEF, FN_0, Seq(FN, List(),
		                            Seq(PRINTLN, "No args call!"))),
		        Seq(IF, true,
		              Seq(PRINTLN, "Hello\nWorld!")),
		        Seq(IF, null,
		              Seq(PRINTLN, "you don't see me"),
		              Seq(PRINTLN, Seq(PRN_STR, Seq(QUOTE, Seq(PRINTLN, "I'm quoted!"))))),
		        Seq(LET, List(BLARG, PRINTLN,
		                      PRINTLN, QUOTE,
						      FN_1, Seq(FN, List(ARG),
		                                 Seq(BLARG, "Passed argument: ", Seq(PRN_STR, ARG)),
		                                 ARG)),
		              Seq(BLARG, Seq(PRINTLN, "\"let\" works!")),
		              Seq(FN_0),
		              Seq(FN_1, "Just passing by.")),
		        Seq(Seq(FN, List(DO, IF, QUOTE, LET, FN),
		                 Seq(PRINTLN, DO, IF, QUOTE, LET, FN)),
		            1, 2, 3, 4, 5));
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
			System.out.println("Failed");
			e.printStackTrace(System.out);
		}
	}
}
