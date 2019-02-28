package de.npecomplete.mc.testproject.lisp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.npecomplete.mc.testproject.lisp.data.CoreLibrary;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.function.LispFunction;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;
import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

// TODO: switch from using java.util.List to own Vector class
// TODO: destructuring
// TODO: macros, loop, recur

public class Lisp {
	public final Environment globalEnv;

	public Lisp() {
		globalEnv = new Environment(null);
	}

	public void initStandardEnvironment() {
		// SPECIAL FORMS
		globalEnv.bind(new Symbol("def"), SpecialForm.DEF);
		globalEnv.bind(new Symbol("do"), SpecialForm.DO);
		globalEnv.bind(new Symbol("fn"), SpecialForm.FN);
		globalEnv.bind(new Symbol("if"), SpecialForm.IF);
		globalEnv.bind(new Symbol("let"), SpecialForm.LET);
		globalEnv.bind(new Symbol("quote"), SpecialForm.QUOTE);

		// EVAL & APPLY
		globalEnv.bind(new Symbol("eval"), new LispFunction() {
			@Override
			public Object apply(Object par1) {
				return eval(par1);
			}
		});
		globalEnv.bind(new Symbol("apply"), CoreLibrary.FN_APPLY);

		// DATA STRUCTURE CREATION
		// TODO replace with interop in some yet-to-be-written core lisp file
		globalEnv.bind(new Symbol("list"), CoreLibrary.FN_LIST);
		globalEnv.bind(new Symbol("vector"), CoreLibrary.FN_VECTOR);
		globalEnv.bind(new Symbol("hash-set"), CoreLibrary.FN_HASH_SET);
		globalEnv.bind(new Symbol("hash-map"), CoreLibrary.FN_HASH_MAP);

		// SEQUENCE INTERACTION
		globalEnv.bind(new Symbol("seq"), CoreLibrary.FN_SEQ);
		globalEnv.bind(new Symbol("first"), CoreLibrary.FN_FIRST);
		globalEnv.bind(new Symbol("next"), CoreLibrary.FN_NEXT);
		globalEnv.bind(new Symbol("rest"), CoreLibrary.FN_REST);

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

		// TODO: COMPARISONS
	}

	public Object eval(Object obj) throws LispException {
		return eval(obj, globalEnv);
	}

	public static Object eval(Object obj, Environment env) throws LispException {
		if (obj instanceof Symbol) {
			return env.lookup((Symbol) obj);
		}

		if (obj instanceof Sequence) {
			Sequence seq = (Sequence) obj;
			if (seq.empty()) {
				throw new LispException("Can't evaluate empty list");
			}
			// evaluate first element
			Object callable = eval(seq.first(), env);

			// call to special form
			if (callable instanceof SpecialForm) {
				Sequence args = seq.more();
				return ((SpecialForm) callable).apply(args, env);
			}

			// call to function
			LispFunction fn = LispFunction.from(callable);
			if (fn != null) {
				Sequence args = seq.more();
				if (args.empty()) {
					return fn.apply(); // no arguments
				}

				Object arg1 = eval(args.first(), env);
				args = args.next();
				if (args == null) {
					return fn.apply(arg1); // one argument
				}

				Object arg2 = eval(args.first(), env);
				args = args.next();
				if (args == null) {
					return fn.apply(arg1, arg2); // two arguments
				}

				Object arg3 = eval(args.first(), env);
				args = args.next();
				if (args == null) {
					return fn.apply(arg1, arg2, arg3); // three arguments
				}

				Object arg4 = eval(args.first(), env);
				args = args.next();
				if (args == null) {
					return fn.apply(arg1, arg2, arg3, arg4); // four arguments
				}

				// more than four arguments
				List<Object> moreArgs = new ArrayList<>(3);
				do {
					moreArgs.add(eval(args.first(), env));
				} while ((args = args.next()) != null);
				return fn.apply(arg1, arg2, arg3, arg4, moreArgs.toArray());
			}

			String call = LispPrinter.printStr(seq);
			String first = LispPrinter.printStr(seq.first());
			throw new LispException("Can't call " + callable + " | "
					+ "Was returned when evaluating: " + first + " | "
					+ "Call: " + call);
		}

		if (obj instanceof List) {
			List<?> list = (List<?>) obj;
			List<Object> result = new ArrayList<>();
			for (Object o : list) {
				result.add(eval(o, env));
			}
			return result;
		}

		if (obj instanceof Set) {
			Set<?> set = (Set<?>) obj;
			Set<Object> result = new HashSet<>(set.size() * 2);
			for (Object o : set) {
				Object key = eval(o, env);
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
				Object key = eval(e.getKey(), env);
				if (result.containsKey(key)) {
					throw new LispException("Map creation with duplicate key: " + key);
				}
				result.put(key, eval(e.getValue(), env));
			}
			return result;
		}

		return obj;
	}
}
