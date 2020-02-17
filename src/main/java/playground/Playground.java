package playground;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.stream.DoubleStream;

import de.npcomplete.nplisp.Lisp;
import de.npcomplete.nplisp.Repl;
import de.npcomplete.nplisp.Var;
import de.npcomplete.nplisp.function.LispFunctionFactory.Fn0;
import de.npcomplete.nplisp.util.LispPrinter;
import de.npcomplete.nplisp.util.LispReader;

public final class Playground {
	private static final Repl repl = new Repl(new Lisp());

	private static final Object inNsPlaygroundForm = repl.evalStr("'(in-ns 'playground)");
	private static final Object defExitForm = repl.evalStr("'(def exit)");

	private static void evalForms(Iterator<Object> forms, boolean isAuto) {// add test helper functions
		repl.eval(inNsPlaygroundForm);
		// TODO: move into test.edn file once interop exists
		Var exitVar = repl.eval(defExitForm);
		exitVar.bind((Fn0) () -> {
			System.exit(0);
			return null;
		});

		if (!isAuto) {
			System.out.print(repl.currentNs().name + "=> ");
			System.out.flush();
		}

		while (forms.hasNext()) {
			try {
				Object form = forms.next();
				if (isAuto) {
					System.out.println();
					System.out.print(repl.currentNs().name + "=> " + form);
					System.out.println();
				}
				Object result = repl.eval(form);
				System.out.println(LispPrinter.prStr(result));
			} catch (Exception e) {
				System.out.println("Failed - " + e.getClass().getName() + ": " + e.getMessage());
			}
			if (!isAuto) {
				System.out.println();
				System.out.print(repl.currentNs().name + "=> ");
				System.out.flush();
			}
		}
	}

	public static void main(String[] args) {
		try (Reader reader = new InputStreamReader(System.in)) {
			evalForms(LispReader.readMany(reader), false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final class SingleRun {
		public static void main(String[] arguments) throws Exception {
			long start = System.nanoTime();
			try {
				evalForms(LispReader.readMany(new FileReader("test.edn")), true);
			} finally {
				long time = System.nanoTime() - start;
				System.out.println("Runtime: " + time / 1_000_000.0 + " ms");
			}
			Thread.sleep(100);
		}
	}

	public static final class PerfCheck {
		public static void main(String[] arguments) throws Exception {
			// read forms ahead of time, to remove that from the timing
			List<Object> forms = new ArrayList<>();
			Iterator<Object> it = LispReader.readMany(new FileReader("test.edn"));
			while (it.hasNext()) {
				forms.add(it.next());
			}

			// disable output
			PrintStream sysout = System.out;
			System.setOut(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					// no-op
				}
			}));

			int count = 50;
			double[] times = new double[count];

			// run a few times
			for (int i = 0; i < count; i++) {
				long start = System.nanoTime();
				evalForms(forms.iterator(), true);
				times[i] = (System.nanoTime() - start) / 1_000_000.0;
			}

			double median =
					DoubleStream.of(times)
							.sorted()
							.skip(count / 2)
							.findFirst()
							.getAsDouble();

			DoubleSummaryStatistics stats = DoubleStream.of(times).summaryStatistics();
			sysout.println("   min: " + stats.getMin() + " ms");
			sysout.println("median: " + median + " ms");
			sysout.println("   avg: " + stats.getAverage() + " ms");
			sysout.println("   max: " + stats.getMax() + " ms");
			sysout.println(" first: " + times[0] + " ms");

			System.setOut(sysout);
			Thread.sleep(100);
		}
	}
}
