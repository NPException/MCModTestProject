package de.npcomplete.nplisp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import de.npcomplete.nplisp.data.Symbol;
import de.npcomplete.nplisp.function.LispFunction;
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
		Lisp lisp = new Lisp();

		Namespace ns = lisp.namespaces.getOrCreateNamespace("nplisp.core");
		Environment env = new Environment(ns, null);

		// add test helper functions

		env.bind(new Symbol("exit"), new LispFunction() {
			@Override
			public Object apply() {
				System.exit(0);
				return null;
			}

			@Override
			public Object apply(Object par1) {
				System.exit(((Number) par1).intValue());
				return null;
			}
		});

		env.bind(new Symbol("reload!"), new LispFunction() {
			@Override
			public Object apply() {
				lisp.reset();
				return null;
			}
		});

		try (Reader reader = new InputStreamReader(in)) {
			Iterator<Object> it = LispReader.readMany(reader);
			while (it.hasNext()) {
				run(env, it.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void run(Environment env, Object form) {
		System.out.println();
		System.out.print("~: ");
		System.out.println(LispPrinter.prStr(form));
		try {
			Object result = Lisp.eval(form, env, false);
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
