package de.npcomplete.nplisp;

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

import de.npcomplete.nplisp.data.CoreLibrary;
import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.data.Symbol;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.function.LispFunctionFactory;
import de.npcomplete.nplisp.function.LispFunctionFactory.Fn1;
import de.npcomplete.nplisp.function.Macro;
import de.npcomplete.nplisp.function.SpecialForm;
import de.npcomplete.nplisp.util.LispPrinter;
import de.npcomplete.nplisp.util.LispReader;

/*

Require entire namespace to be within a 'ns' form. That will give a cleaner option for
multiple namespaces in the same file.
Example:

(ns my-project.core
  (require '[vector.math :as vm])

  ...
  regular code here
  ...
  )

 */

// TODO: finish namespaces: implement 'ns' and 'require'
// TODO: javadoc in CoreLibrary
// TODO: switch from using java.util.List to own Vector class
// TODO: destructuring
// TODO: proper macro expansion
// TODO: syntax-quote / unquote
// TODO: loop
// TODO: doc-strings

public class Lisp {
	public static final String CORE_NS_NAME = "nplisp.core";
	public final NamespaceMap namespaces = new NamespaceMap();

	public Lisp() {
		// INIT CORE LIBRARY //

		System.out.println("Initializing core library");
		long start = System.nanoTime();

		Namespace coreNs = namespaces.core;

		// SPECIAL FORMS
		def(coreNs, "def", (SpecialForm) SpecialForm::DEF);
		def(coreNs, "do", (SpecialForm) SpecialForm::DO);
		def(coreNs, "fn", (SpecialForm) SpecialForm::FN);
		def(coreNs, "if", (SpecialForm) SpecialForm::IF);
		def(coreNs, "let", (SpecialForm) SpecialForm::LET);
		def(coreNs, "var", (SpecialForm) SpecialForm::VAR);
		def(coreNs, "quote", (SpecialForm) SpecialForm::QUOTE);
		def(coreNs, "defmacro", (SpecialForm) SpecialForm::DEFMACRO);

		// LOOP (TODO)
		def(coreNs, "recur", CoreLibrary.FN_RECUR);

		// EVAL & APPLY
		def(coreNs, "eval", (Fn1) this::eval);
		def(coreNs, "apply", CoreLibrary.FN_APPLY);

		// DATA STRUCTURE CREATION
		def(coreNs, "list", CoreLibrary.FN_LIST);
		def(coreNs, "vector", CoreLibrary.FN_VECTOR);
		def(coreNs, "hash-set", CoreLibrary.FN_HASH_SET);
		def(coreNs, "hash-map", CoreLibrary.FN_HASH_MAP);

		// SEQUENCE INTERACTION
		def(coreNs, "seq", CoreLibrary.FN_SEQ);
		def(coreNs, "first", CoreLibrary.FN_FIRST);
		def(coreNs, "next", CoreLibrary.FN_NEXT);
		def(coreNs, "rest", CoreLibrary.FN_REST);
		def(coreNs, "cons", CoreLibrary.FN_CONS);

		// MATHS
		def(coreNs, "+", CoreLibrary.FN_ADD);
		def(coreNs, "-", CoreLibrary.FN_SUBTRACT);
		def(coreNs, "*", CoreLibrary.FN_MULTIPLY);
		def(coreNs, "/", CoreLibrary.FN_DIVIDE);

		// STRING, SYMBOL, AND KEYWORD INTERACTION
		def(coreNs, "str", CoreLibrary.FN_STR);
		def(coreNs, "name", CoreLibrary.FN_NAME);
		def(coreNs, "symbol", CoreLibrary.FN_SYMBOL);
		def(coreNs, "keyword", CoreLibrary.FN_KEYWORD);

		// PRINTING
		def(coreNs, "pr", CoreLibrary.FN_PR);
		def(coreNs, "prn", CoreLibrary.FN_PRN);
		def(coreNs, "pr-str", CoreLibrary.FN_PR_STR);
		def(coreNs, "prn-str", CoreLibrary.FN_PRN_STR);

		def(coreNs, "print", CoreLibrary.FN_PRINT);
		def(coreNs, "println", CoreLibrary.FN_PRINTLN);
		def(coreNs, "print-str", CoreLibrary.FN_PRINT_STR);
		def(coreNs, "println-str", CoreLibrary.FN_PRINTLN_STR);

		// NAMESPACE HANDLING
		def(coreNs, "*ns*", coreNs); // *ns* holds the current namespace for calls to 'eval'
		def(coreNs, "in-ns", (Fn1) par -> {
			String name = (String) CoreLibrary.FN_NAME.apply(par);
			Namespace ns = namespaces.getOrCreateNamespace(name);
			currentNsVar().bind(ns);
			return ns;
		});
		// TODO: implement 'ns' and 'require'

		// TODO: COMPARISONS

		// UTILITY
		def(coreNs, "time", CoreLibrary.MACRO_TIME);

		// bootstrap rest of core library

		try (InputStream in = Lisp.class.getResourceAsStream("core.edn");
			 Reader reader = new InputStreamReader(in)) {
			Environment coreEnv = new Environment(coreNs, null);
			Iterator<Object> it = LispReader.readMany(reader);
			while (it.hasNext()) {
				eval(it.next(), coreEnv, false);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to load core library", e);
		}

		// setup default 'user' namespace
		Namespace ns = namespaces.getOrCreateNamespace("user");
		currentNsVar().bind(ns);

		long time = System.nanoTime() - start;
		System.out.println("Done in " + time / 1_000_000.0 + " msecs");
	}

	private Var currentNsVar() {
		return namespaces.core.lookupVar(new Symbol("*ns*"));
	}

	public Object eval(Object obj) {
		Namespace currentNs = (Namespace) currentNsVar().deref();
		return eval(obj, new Environment(currentNs, null), false);
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

			// TODO: proper macro expansion phase (try to expand macros after reading)
			// TODO: alternatively replace current sequence in code after expansion
			if (callable instanceof Macro) {
				Sequence args = seq.more();
				Object expansion = ((Macro) callable).expand(args);
				return eval(expansion, env, allowRecur);
			}

			// call to function
			LispFunction fn = LispFunctionFactory.from(callable);
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

				// more than three arguments
				List<Object> moreArgs = new ArrayList<>(3);
				do {
					moreArgs.add(eval(args.first(), env, false));
				} while ((args = args.next()) != null);
				return check(allowRecur, fn.apply(arg1, arg2, arg3, moreArgs.toArray()));
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
			for (Entry<?, ?> e : map.entrySet()) {
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
		if (!allowRecur && val instanceof CoreLibrary.TailCall) {
			throw new LispException("Illegal call to 'recur'. Can only be used in function tail position.");
		}
		return val;
	}

	private static void def(Namespace ns, String key, Object value) {
		ns.defineVar(new Symbol(key)).bind(value);
	}

	public static class NamespaceMap {
		private final Map<String, Namespace> namespaces = new HashMap<>();
		private final Map<String, Map<String, Var>> allVars = new HashMap<>();
		final Namespace core = new Namespace(CORE_NS_NAME, null, this::internVar);

		NamespaceMap() {
			namespaces.put(CORE_NS_NAME, core);
		}

		private Var internVar(Symbol symbol) {
			if (symbol.nsName == null) {
				throw new LispException("Can't intern a Var for a symbol without a namespace");
			}
			Map<String, Var> nsVars = allVars.computeIfAbsent(symbol.nsName, k -> new HashMap<>());
			return nsVars.computeIfAbsent(symbol.name, k -> new Var(symbol));
		}

		public Namespace getOrCreateNamespace(String name) {
			return namespaces.computeIfAbsent(name, k -> new Namespace(k, core, this::internVar));
		}
	}
}
