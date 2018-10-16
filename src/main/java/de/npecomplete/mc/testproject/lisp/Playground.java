package de.npecomplete.mc.testproject.lisp;

import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;
import de.npecomplete.mc.testproject.lisp.util.LispElf;
import de.npecomplete.mc.testproject.lisp.util.LispPrinter;
import de.npecomplete.mc.testproject.lisp.util.LispReader;

public class Playground {
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

		lisp.globalEnv.bind(new Symbol("println"), printlnForm);
		lisp.globalEnv.bind(new Symbol("prn-str"), prnStrForm);

		String formStr ="(do\n" +
				"  (def fn-0 (fn [] (println \"No args call!\")))\n" +
				"  (if true (println \"Hello\\nWorld!\"))\n" +
				"  (if null\n" +
				"    (println \"you don't see me\")\n" +
				"    (println (prn-str (quote (println \"I'm quoted!\")))))\n" +
				"  (let [blarg println\n" +
				"        println quote\n" +
				"        fn-1 (fn [arg]\n" +
				"               (blarg \"Passed argument: \" (prn-str arg))\n" +
				"               arg)]\n" +
				"    (blarg (println (let works !)))\n" +
				"    (fn-0)\n" +
				"    (fn-1 \"Just passing by.\"))\n" +
				"  ((fn [do if quote let fn]\n" +
				"     (println do if quote let fn))\n" +
				"\t1 2 3 4 5)\n" +
				"  (def multi\n" +
				"    (fn ([] (println \"Nothing to see.\"))\n" +
				"\t    ([x] (println \"Something to see: \" x))))\n" +
				"  (multi)\n" +
				"  (multi \"FooBar!\")\n" +
				"  (println (#{:test} :test))\n" +
				"  (println ({:key :value} :key)))";

		run(lisp, LispReader.readStr(formStr));

		run(lisp, LispReader.readStr("is-dead"));
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
