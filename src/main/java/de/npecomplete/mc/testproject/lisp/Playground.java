package de.npecomplete.mc.testproject.lisp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

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

		// TODO: switch from using java.util.List to own Vector class
		// TODO: use "let" in function code for bindings and execution
		// TODO: var-arg functions, fn names, eval, apply, macros, recur, loop

		try (InputStream in = Playground.class.getResourceAsStream("/test.edn");
//		try (InputStream in = System.in;
			 Reader reader = new InputStreamReader(in)) {
			Iterator<Object> it = LispReader.readMany(reader);
			while (it.hasNext()) {
				run(lisp, it.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		run(lisp, LispReader.readStr("is-dead"));
	}

	private static void run(Lisp lisp, Object form) {
		System.out.println();
		System.out.print("~: ");
		System.out.println(LispPrinter.printStr(form));
		try {
			Object result = lisp.eval(form);
			System.out.print("~>");
			System.out.println(LispPrinter.printStr(result));
		} catch (Exception e) {
			System.out.println("Failed");
			e.printStackTrace(System.out);
		}
	}
}
