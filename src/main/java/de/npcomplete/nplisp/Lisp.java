package de.npcomplete.nplisp;

import static de.npcomplete.nplisp.Environment.SYM_CURRENT_NAMESPACE;

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
import java.util.function.Function;

import de.npcomplete.nplisp.data.Cons;
import de.npcomplete.nplisp.data.CoreLibrary;
import de.npcomplete.nplisp.data.Namespace;
import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.data.Symbol;
import de.npcomplete.nplisp.data.Var;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.function.Macro;
import de.npcomplete.nplisp.function.SpecialForm;
import de.npcomplete.nplisp.util.LispPrinter;
import de.npcomplete.nplisp.util.LispReader;

/*

Maybe require entire namespace to be within a namespace form. That would give a cleaner option for
multiple namespaces in the same file.
Example:

(ns my-project.core
  (require '[vector.math :as vm])

  ...
  regular code here
  ...
  )

 */

// TODO: fix equals/hashcode of Symbol and Keyword (include namespace)
// TODO: read namespaced symbols and keywords
// TODO: finish namespaces
// TODO: reader macro for vars: #'my-symbol
// TODO: javadoc in CoreLibrary
// TODO: switch from using java.util.List to own Vector class
// TODO: destructuring
// TODO: proper macro expansion
// TODO: syntax-quote / unquote
// TODO: loop
// TODO: doc-strings

public class Lisp {
	private final Map<String, Namespace> namespaces = new HashMap<>();
	public final Environment globalEnv;

	public Lisp() {
		globalEnv = new Environment(null);
	}

	public void initStandardEnvironment() {
		// INIT CORE LIBRARY //

		System.out.println("Initializing core library");
		long start = System.nanoTime();

		// TODO: Use dummy env when building core to avoid global env bindings to all core functions.
		//       Or (perhaps better) give root environment to every namespace.
		//       We may be able to completely remove the bindings map in Namespace.
		//       Will need to enforce fully qualified symbols for Var though.

		setNamespace(globalEnv, "core");

		// SPECIAL FORMS
		def(globalEnv, "def", (SpecialForm) SpecialForm::DEF);
		def(globalEnv, "do", (SpecialForm) SpecialForm::DO);
		def(globalEnv, "fn", (SpecialForm) SpecialForm::FN);
		def(globalEnv, "if", (SpecialForm) SpecialForm::IF);
		def(globalEnv, "let", (SpecialForm) SpecialForm::LET);
		def(globalEnv, "var", (SpecialForm) SpecialForm::VAR);
		def(globalEnv, "quote", (SpecialForm) SpecialForm::QUOTE);
		def(globalEnv, "defmacro", (SpecialForm) SpecialForm::DEFMACRO);

		// LOOP (TODO)
		def(globalEnv, "recur", CoreLibrary.FN_RECUR);

		// EVAL & APPLY
		def(globalEnv, "eval", LispFunction.from((Function<?, ?>) this::eval));
		def(globalEnv, "apply", CoreLibrary.FN_APPLY);

		// DATA STRUCTURE CREATION
		def(globalEnv, "list", CoreLibrary.FN_LIST);
		def(globalEnv, "vector", CoreLibrary.FN_VECTOR);
		def(globalEnv, "hash-set", CoreLibrary.FN_HASH_SET);
		def(globalEnv, "hash-map", CoreLibrary.FN_HASH_MAP);

		// SEQUENCE INTERACTION
		def(globalEnv, "seq", CoreLibrary.FN_SEQ);
		def(globalEnv, "first", CoreLibrary.FN_FIRST);
		def(globalEnv, "next", CoreLibrary.FN_NEXT);
		def(globalEnv, "rest", CoreLibrary.FN_REST);
		def(globalEnv, "cons", CoreLibrary.FN_CONS);

		// MATHS
		def(globalEnv, "+", CoreLibrary.FN_ADD);
		def(globalEnv, "-", CoreLibrary.FN_SUBTRACT);
		def(globalEnv, "*", CoreLibrary.FN_MULTIPLY);
		def(globalEnv, "/", CoreLibrary.FN_DIVIDE);

		// STRING, SYMBOL, AND KEYWORD INTERACTION
		def(globalEnv, "str", CoreLibrary.FN_STR);
		def(globalEnv, "name", CoreLibrary.FN_NAME);
		def(globalEnv, "symbol", CoreLibrary.FN_SYMBOL);
		def(globalEnv, "keyword", CoreLibrary.FN_KEYWORD);

		// PRINTING
		def(globalEnv, "pr", CoreLibrary.FN_PR);
		def(globalEnv, "prn", CoreLibrary.FN_PRN);
		def(globalEnv, "pr-str", CoreLibrary.FN_PR_STR);
		def(globalEnv, "prn-str", CoreLibrary.FN_PRN_STR);

		def(globalEnv, "print", CoreLibrary.FN_PRINT);
		def(globalEnv, "println", CoreLibrary.FN_PRINTLN);
		def(globalEnv, "print-str", CoreLibrary.FN_PRINT_STR);
		def(globalEnv, "println-str", CoreLibrary.FN_PRINTLN_STR);

		// TODO: COMPARISONS

		// UTILITY
		def(globalEnv, "time", CoreLibrary.MACRO_TIME);

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
			return env.lookup((Symbol) obj).deref();
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
		if (allowRecur || !(val instanceof CoreLibrary.TailCall)) {
			return val;
		}
		throw new LispException("Illegal call to 'recur'. Can only be used in function tail position.");
	}

	private Namespace setNamespace(Environment env, String name) {
		Namespace ns = namespaces.computeIfAbsent(name, Namespace::new);
		Var currentNsVar = new Var(null, SYM_CURRENT_NAMESPACE.name).bindValue(ns);
		env.bindVar(SYM_CURRENT_NAMESPACE, currentNsVar);
		return ns;
	}

	private static void def(Environment env, String name, Object value) {
		// replace with manual code that does not bind the value to a the environment which holds *ns*
		Sequence args = new Cons(new Symbol(name), new Cons(value, null));
		SpecialForm.DEF(args, env, false);
	}
}
