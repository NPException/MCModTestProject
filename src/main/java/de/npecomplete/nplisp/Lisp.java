package de.npecomplete.nplisp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.npecomplete.nplisp.data.CoreLibrary;
import de.npecomplete.nplisp.data.Sequence;
import de.npecomplete.nplisp.data.Symbol;
import de.npecomplete.nplisp.function.LispFunction;
import de.npecomplete.nplisp.function.VarArgsFunction;
import de.npecomplete.nplisp.special.SpecialForm;
import de.npecomplete.nplisp.util.LispPrinter;
import de.npecomplete.nplisp.util.LispReader;

// TODO: javadoc in CoreLibrary
// TODO: switch from using java.util.List to own Vector class
// TODO: destructuring
// TODO: macros, loop

public class Lisp {
	public final Environment globalEnv;

	public Lisp() {
		globalEnv = new Environment(null);
	}

	public void initStandardEnvironment() {
		System.out.println("Initializing core library");
		long start = System.nanoTime();

		// SPECIAL FORMS
		globalEnv.bind(new Symbol("def"), SpecialForm.DEF);
		globalEnv.bind(new Symbol("do"), SpecialForm.DO);
		globalEnv.bind(new Symbol("fn"), SpecialForm.FN);
		globalEnv.bind(new Symbol("if"), SpecialForm.IF);
		globalEnv.bind(new Symbol("let"), SpecialForm.LET);
		globalEnv.bind(new Symbol("quote"), SpecialForm.QUOTE);

		// LOOP (TODO)
		globalEnv.bind(new Symbol("recur"), CoreLibrary.FN_RECUR);

		// EVAL & APPLY
		globalEnv.bind(new Symbol("eval"), new LispFunction() {
			@Override
			public Object apply(Object par1) {
				return eval(par1);
			}
		});
		globalEnv.bind(new Symbol("apply"), CoreLibrary.FN_APPLY);

		// DATA STRUCTURE CREATION
		globalEnv.bind(new Symbol("list"), CoreLibrary.FN_LIST);
		globalEnv.bind(new Symbol("vector"), CoreLibrary.FN_VECTOR);
		globalEnv.bind(new Symbol("hash-set"), CoreLibrary.FN_HASH_SET);
		globalEnv.bind(new Symbol("hash-map"), CoreLibrary.FN_HASH_MAP);

		// SEQUENCE INTERACTION
		globalEnv.bind(new Symbol("seq"), CoreLibrary.FN_SEQ);
		globalEnv.bind(new Symbol("first"), CoreLibrary.FN_FIRST);
		globalEnv.bind(new Symbol("next"), CoreLibrary.FN_NEXT);
		globalEnv.bind(new Symbol("rest"), CoreLibrary.FN_REST);
		globalEnv.bind(new Symbol("cons"), CoreLibrary.FN_CONS);

		// MATHS
		globalEnv.bind(new Symbol("+"), CoreLibrary.FN_ADD);
		globalEnv.bind(new Symbol("-"), CoreLibrary.FN_SUBTRACT);
		globalEnv.bind(new Symbol("*"), CoreLibrary.FN_MULTIPLY);
		globalEnv.bind(new Symbol("/"), CoreLibrary.FN_DIVIDE);

		// STRING, SYMBOL, AND KEYWORD INTERACTION
		globalEnv.bind(new Symbol("str"), CoreLibrary.FN_STR);
		globalEnv.bind(new Symbol("name"), CoreLibrary.FN_NAME);
		globalEnv.bind(new Symbol("symbol"), CoreLibrary.FN_SYMBOL);
		globalEnv.bind(new Symbol("keyword"), CoreLibrary.FN_KEYWORD);

		// PRINTING
		globalEnv.bind(new Symbol("pr"), CoreLibrary.FN_PR);
		globalEnv.bind(new Symbol("prn"), CoreLibrary.FN_PRN);
		globalEnv.bind(new Symbol("pr-str"), CoreLibrary.FN_PR_STR);
		globalEnv.bind(new Symbol("prn-str"), CoreLibrary.FN_PRN_STR);

		globalEnv.bind(new Symbol("print"), CoreLibrary.FN_PRINT);
		globalEnv.bind(new Symbol("println"), CoreLibrary.FN_PRINTLN);
		globalEnv.bind(new Symbol("print-str"), CoreLibrary.FN_PRINT_STR);
		globalEnv.bind(new Symbol("println-str"), CoreLibrary.FN_PRINTLN_STR);

		// TODO: COMPARISONS

		// UTILITY
		globalEnv.bind(new Symbol("time"), CoreLibrary.MACRO_TIME);
		globalEnv.bind(new Symbol("exit"), (VarArgsFunction) args -> {
			System.exit(0);
			return null;
		});

		// bootstrap rest of core library

		try (InputStream in = Lisp.class.getResourceAsStream("core.edn");
				Reader reader = new InputStreamReader(in)) {
			Iterator<Object> it = LispReader.readMany(reader);
			while (it.hasNext()) {
				eval(it.next());
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to load core library", e);
		}

		long time = System.nanoTime() - start;
		System.out.println("Done in " + time / 1_000_000.0 + " msecs");
	}

	public Object eval(Object obj) throws LispException {
		return eval(obj, globalEnv, false);
	}

	public static Object eval(Object obj, Environment env, boolean allowRecur) throws LispException {
		if (obj instanceof Symbol) {
			return env.lookup((Symbol) obj);
		}

		if (obj instanceof Sequence) {
			Sequence seq = (Sequence) obj;
			if (seq.empty()) {
				throw new LispException("Can't evaluate empty list");
			}
			// evaluate first element
			Object callable = eval(seq.first(), env, false);

			// call to special form
			if (callable instanceof SpecialForm) {
				Sequence args = seq.more();
				return check(allowRecur, ((SpecialForm) callable).apply(args, env, allowRecur));
			}

			// call to function
			LispFunction fn = LispFunction.from(callable);
			if (fn != null) {
				Sequence args = seq.more();
				if (args.empty()) {
					return check(allowRecur, fn.apply()); // no arguments
				}

				Object arg1 = eval(args.first(), env, false);
				args = args.next();
				if (args == null) {
					return check(allowRecur, fn.apply(arg1)); // one argument
				}

				Object arg2 = eval(args.first(), env, false);
				args = args.next();
				if (args == null) {
					return check(allowRecur, fn.apply(arg1, arg2)); // two arguments
				}

				Object arg3 = eval(args.first(), env, false);
				args = args.next();
				if (args == null) {
					return check(allowRecur, fn.apply(arg1, arg2, arg3)); // three arguments
				}

				Object arg4 = eval(args.first(), env, false);
				args = args.next();
				if (args == null) {
					return check(allowRecur, fn.apply(arg1, arg2, arg3, arg4)); // four arguments
				}

				// more than four arguments
				List<Object> moreArgs = new ArrayList<>(3);
				do {
					moreArgs.add(eval(args.first(), env, false));
				} while ((args = args.next()) != null);
				return check(allowRecur, fn.apply(arg1, arg2, arg3, arg4, moreArgs.toArray()));
			}

			String call = LispPrinter.prStr(seq);
			String first = LispPrinter.prStr(seq.first());
			throw new LispException("Can't call " + callable + " | "
					+ "Was returned when evaluating: " + first + " | "
					+ "Call: " + call);
		}

		if (obj instanceof List) {
			List<?> list = (List<?>) obj;
			List<Object> result = new ArrayList<>();
			for (Object o : list) {
				result.add(eval(o, env, false));
			}
			return result;
		}

		if (obj instanceof Set) {
			Set<?> set = (Set<?>) obj;
			Set<Object> result = new HashSet<>(set.size() * 2);
			for (Object o : set) {
				Object key = eval(o, env, false);
				if (result.contains(key)) {
					throw new LispException("Set creation with duplicate key: " + key);
				}
				result.add(key);
			}
			return result;
		}

		if (obj instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) obj;
			Map<Object, Object> result = new HashMap<>(map.size() * 2);
			for (Entry e : map.entrySet()) {
				Object key = eval(e.getKey(), env, false);
				if (result.containsKey(key)) {
					throw new LispException("Map creation with duplicate key: " + key);
				}
				result.put(key, eval(e.getValue(), env, false));
			}
			return result;
		}

		return obj;
	}

	private static Object check(boolean allowRecur, Object val) {
		if (allowRecur || !(val instanceof CoreLibrary.TailCall)) {
			return val;
		}
		throw new LispException("Illegal call to 'recur'. Can only be used in function tail position.");
	}
}
