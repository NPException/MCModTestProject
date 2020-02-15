package playground;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import de.npcomplete.nplisp.Lisp;
import de.npcomplete.nplisp.Repl;
import de.npcomplete.nplisp.Var;
import de.npcomplete.nplisp.function.LispFunctionFactory.Fn0;
import de.npcomplete.nplisp.util.LispPrinter;
import de.npcomplete.nplisp.util.LispReader;

public final class Playground {

	public static void main(String[] arguments) throws Exception {
		long start = System.nanoTime();
		try {
			start(new FileInputStream("test.edn"));
		} finally {
			long time = System.nanoTime() - start;
			System.out.println("Runtime: " + time + " ns");
		}
		Thread.sleep(100);
	}

	private static void start(InputStream in) {
		Repl repl = new Repl(new Lisp());
		// add test helper functions

		repl.evalStr("(in-ns 'playground)");

		// TODO: move into test.edn file once interop exists
		Var exitVar = repl.evalStr("(def exit)");
		exitVar.bind((Fn0) () -> {
			System.exit(0);
			return null;
		});

		try (Reader reader = new InputStreamReader(in)) {
			Iterator<Object> it = LispReader.readMany(reader);
			while (it.hasNext()) {
				run(repl, it.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void run(Repl repl, Object form) {
		System.out.println();
		System.out.print("~: ");
		System.out.println(LispPrinter.prStr(form));
		try {
			Object result = repl.eval(form);
			System.out.print("~>");
			System.out.println(LispPrinter.prStr(result));
		} catch (Exception e) {
			System.out.println("Failed - " + e.getClass().getName() + ": " + e.getMessage());
		}
	}

	public static final class REPL {
		public static void main(String[] args) {
			start(System.in);
		}
	}
}
