package de.npcomplete.nplisp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import de.npcomplete.nplisp.core.Environment;
import de.npcomplete.nplisp.core.Namespace;
import de.npcomplete.nplisp.core.Var;
import de.npcomplete.nplisp.corelibrary.CoreLibrary;
import de.npcomplete.nplisp.corelibrary.CoreLibrary.TailCall;
import de.npcomplete.nplisp.corelibrary.SyntaxQuote;
import de.npcomplete.nplisp.corelibrary.Concat;
import de.npcomplete.nplisp.data.Delay;
import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.data.Symbol;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.function.LispFunctionFactory;
import de.npcomplete.nplisp.function.LispFunctionFactory.Fn1;
import de.npcomplete.nplisp.function.LispFunctionFactory.Fn2;
import de.npcomplete.nplisp.function.Macro;
import de.npcomplete.nplisp.function.SpecialForm;
import de.npcomplete.nplisp.function.VarArgsFunction;
import de.npcomplete.nplisp.util.LispPrinter;

// TODO: doc-strings
// TODO: interop - (. $target-class $receiver-instance ($method-name $arg*))
//                 (. $target-class $receiver-instance $field-name) // get
//                 (. $target-class $receiver-instance $field-name $value) // set
// TODO: 'try/catch/finally' form
// TODO: 'with-open' form
// TODO: javadoc in CoreLibrary
// TODO: proper macro expansion
// TODO: ensure all used symbols are already bound when invoking 'fn' or 'defmacro'
// TODO: destructuring
// TODO: switch from using java.util.List to own Vector class

public class Lisp {
	static final Var CORE_EVAL_VAR = new Var(new Symbol("nplisp.core/eval")).markFixed();
	static final Var CORE_CURRENT_NS_VAR = new Var(new Symbol("nplisp.core/*ns*")).markFixed();
	static final Var CORE_SYNTAX_QUOTE_VAR = new Var(new Symbol("nplisp.core/syntax-quote")).macro(true).markFixed();

	private static final Symbol CORE_SYNTAX_QUOTE_STAR_SYM = new Symbol("nplisp.core/syntax-quote*");

	public final NamespaceMap namespaces = new NamespaceMap();

	public Lisp(File libFolder) {
		// INIT CORE LIBRARY //
		// TODO: move initialization to CoreLibrary class

		long start = System.nanoTime();

		Namespace coreNs = namespaces.core;

		def(coreNs, "apply", CoreLibrary.FN_APPLY);

		// SPECIAL FORMS
		def(coreNs, "def", (SpecialForm) SpecialForm::DEF);
		def(coreNs, "do", (SpecialForm) SpecialForm::DO);
		def(coreNs, "fn", (SpecialForm) SpecialForm::FN);
		def(coreNs, "if", (SpecialForm) SpecialForm::IF);
		def(coreNs, "let", (SpecialForm) SpecialForm::LET);
		def(coreNs, "var", (SpecialForm) SpecialForm::VAR);
		def(coreNs, "quote", (SpecialForm) SpecialForm::QUOTE);
		def(coreNs, "syntax-quote*", (Fn2) SyntaxQuote::syntaxQuoteStar);

		def(coreNs, "loop", (SpecialForm) SpecialForm::LOOP);
		def(coreNs, "recur", CoreLibrary.FN_RECUR);

		def(coreNs, "delay", CoreLibrary.SF_DELAY);
		def(coreNs, "deref", CoreLibrary.FN_DEREF);
		def(coreNs, "force", (Fn1) Delay::force);

		// DATA STRUCTURE CREATION
		def(coreNs, "list", (VarArgsFunction) CoreLibrary::list);
		def(coreNs, "vector", (VarArgsFunction) CoreLibrary::vector);
		def(coreNs, "hash-set", (VarArgsFunction) CoreLibrary::hashSet);
		def(coreNs, "hash-map", (VarArgsFunction) CoreLibrary::hashMap);

		// SEQUENCE INTERACTION
		def(coreNs, "seq", (Fn1) CoreLibrary::seq);
		def(coreNs, "first", CoreLibrary.FN_FIRST);
		def(coreNs, "next", CoreLibrary.FN_NEXT);
		def(coreNs, "rest", CoreLibrary.FN_REST);
		def(coreNs, "count", (Fn1) CoreLibrary::count);
		def(coreNs, "cons", (Fn2) CoreLibrary::cons);
		def(coreNs, "concat*", (Fn2) Concat::concat);

		// MATHS
		def(coreNs, "+", CoreLibrary.FN_ADD);
		def(coreNs, "-", CoreLibrary.FN_SUBTRACT);
		def(coreNs, "*", CoreLibrary.FN_MULTIPLY);
		def(coreNs, "/", CoreLibrary.FN_DIVIDE);

		// STRING, SYMBOL, AND KEYWORD INTERACTION
		def(coreNs, "str", (VarArgsFunction) CoreLibrary::str);
		def(coreNs, "name", (Fn1) CoreLibrary::name);
		def(coreNs, "symbol", CoreLibrary.FN_SYMBOL);
		def(coreNs, "keyword", CoreLibrary.FN_KEYWORD);

		// I/O
		def(coreNs, "as-file", CoreLibrary.FN_AS_FILE);
		def(coreNs, "as-url", CoreLibrary.FN_AS_URL);
		def(coreNs, "resource-url", CoreLibrary.FN_RESOURCE_URL);
		def(coreNs, "reader", CoreLibrary.FN_READER);

		// PRINTING
		def(coreNs, "pr", CoreLibrary.FN_PR);
		def(coreNs, "prn", CoreLibrary.FN_PRN);
		def(coreNs, "pr-str", CoreLibrary.FN_PR_STR);
		def(coreNs, "prn-str", CoreLibrary.FN_PRN_STR);

		def(coreNs, "print", CoreLibrary.FN_PRINT);
		def(coreNs, "println", CoreLibrary.FN_PRINTLN);
		def(coreNs, "print-str", CoreLibrary.FN_PRINT_STR);
		def(coreNs, "println-str", CoreLibrary.FN_PRINTLN_STR);

		// READING

		def(coreNs, "read", CoreLibrary.FN_READ);
		def(coreNs, "read-string", CoreLibrary.FN_READ_STRING);

		// NAMESPACE HANDLING

		def(coreNs, "ns", CoreLibrary.SF_NS(namespaces::getOrCreateNamespace));
		def(coreNs, "require", CoreLibrary.SF_REQUIRE(libFolder, namespaces::getNamespace));
		def(coreNs, "import", CoreLibrary.SF_IMPORT);

		// PREDICATES
		def(coreNs, "nil?", (Fn1) Objects::isNull);
		def(coreNs, "some?", (Fn1) Objects::nonNull);
		def(coreNs, "symbol?", (Fn1) CoreLibrary::isSymbol);
		def(coreNs, "keyword?", (Fn1) CoreLibrary::isKeyword);
		def(coreNs, "seqable?", (Fn1) CoreLibrary::isSeqable);
		def(coreNs, "seq?", (Fn1) CoreLibrary::isSeq);
		def(coreNs, "vector?", (Fn1) CoreLibrary::isVector);
		def(coreNs, "set?", (Fn1) CoreLibrary::isSet);
		def(coreNs, "map?", (Fn1) CoreLibrary::isMap);

		// TODO: COMPARISONS
		def(coreNs, "equals", (Fn2) Objects::equals); // TODO: replace with interop when available

		// TODO: INTEROP
		def(coreNs, ".", (SpecialForm) (args, env, allowRecur) -> {
			throw new LispException("Interop not yet implemented.");
		});
		def(coreNs, "instance?", (Fn2) (c, x) -> ((Class<?>) c).isInstance(x));

		// UTILITY
		def(coreNs, "time", CoreLibrary.SF_TIME);

		// bootstrap rest of core library
		Environment coreEnv = new Environment(coreNs);
		eval(CoreLibrary.CORE_NS_FORM.deref(), coreEnv, false);

		// Note: Initialization is quite slow when all the lambdas are first bootstrapped and
		//       and the core namespace is read from disk once. (~30 ms)
		//       After that it's okay. (~0.5 ms)
		long time = System.nanoTime() - start;
		System.out.println("Core library initialized in " + time / 1_000_000.0 + " msecs");
	}

	private static Object lookup(Environment env, Symbol sym, boolean allowMacro) {
		Object val = env.lookup(sym);
		if (!(val instanceof Var)) {
			return val;
		}
		Var var = (Var) val;
		if (var.isMacro()) {
			if (!allowMacro) {
				throw new LispException("Can't take value of a macro: " + var);
			}
			if (var == CORE_SYNTAX_QUOTE_VAR) {
				Namespace ns = env.namespace;
				Var sqFnVar = ns.lookupVar(CORE_SYNTAX_QUOTE_STAR_SYM, true, false);
				return (Macro) args -> {
					if (args.next() != null) {
						throw new LispException("syntax-quote only takes a single argument");
					}
					return sqFnVar.apply(args.first(), ns);
				};
			}
			LispFunction macroFunction = LispFunctionFactory.from(var.deref());
			return (Macro) macroFunction::applyTo;
		}
		// generate 'eval' function which uses the current namespace for evaluation
		if (var == CORE_EVAL_VAR) {
			Namespace ns = env.namespace;
			return (Fn1) par -> eval(par, new Environment(ns), false);
		}
		if (var == CORE_CURRENT_NS_VAR) {
			return env.namespace;
		}
		return var.deref();
	}

	public static Object eval(Object obj, Environment env, boolean allowRecur) throws LispException {
		if (obj instanceof Symbol) {
			return lookup(env, (Symbol) obj, false);
		}

		if (obj instanceof Sequence) {
			Sequence seq = (Sequence) obj;
			if (seq.empty()) {
				throw new LispException("Can't evaluate empty list");
			}
			// evaluate first element with special handling for symbols to allow macros
			Object firstElement = seq.first();
			Object callable = firstElement instanceof Symbol
					? lookup(env, (Symbol) firstElement, true)
					: eval(seq.first(), env, false);

			// call to special form
			if (callable instanceof SpecialForm) {
				Sequence args = seq.more();
				return check(allowRecur, ((SpecialForm) callable).apply(args, env, allowRecur));
			}

			// TODO: proper macro expansion phase (try to expand macros after reading)
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
		if (!allowRecur && val instanceof TailCall) {
			throw new LispException("Illegal call to 'recur'. Can only be used in function tail position.");
		}
		return val;
	}

	private static void def(Namespace ns, String key, Object value) {
		ns.define(new Symbol(key)).bind(value);
	}

	public static class NamespaceMap {
		private final Map<String, Namespace> namespaces = new HashMap<>();
		private final Map<String, Map<String, Var>> allVars = new HashMap<>();
		final Namespace core = new Namespace("nplisp.core", null, this::internVar);

		NamespaceMap() {
			namespaces.put(core.name, core);
			// prepare special vars, so they get can be loaded into the core namespace
			// when 'def' is called for them
			HashMap<String, Var> coreVars = new HashMap<>();
			coreVars.put("eval", CORE_EVAL_VAR);
			coreVars.put("*ns*", CORE_CURRENT_NS_VAR);
			coreVars.put("syntax-quote", CORE_SYNTAX_QUOTE_VAR);
			allVars.put(core.name, coreVars);
		}

		private Var internVar(Symbol symbol) {
			if (symbol.nsName == null) {
				throw new LispException("Can't intern a Var for a symbol without a namespace");
			}
			Map<String, Var> nsVars = allVars.computeIfAbsent(symbol.nsName, k -> new HashMap<>());
			return nsVars.computeIfAbsent(symbol.name, k -> new Var(symbol));
		}

		public Namespace getNamespace(String name) {
			return namespaces.get(name);
		}

		public Namespace getOrCreateNamespace(String name) {
			return namespaces.computeIfAbsent(name, k -> new Namespace(k, core, this::internVar));
		}
	}
}
