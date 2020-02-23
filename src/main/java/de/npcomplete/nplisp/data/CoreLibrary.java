package de.npcomplete.nplisp.data;

import static de.npcomplete.nplisp.data.Sequence.EMPTY_SEQUENCE;
import static de.npcomplete.nplisp.util.LispElf.isSimpleSymbol;
import static de.npcomplete.nplisp.util.LispElf.mapIterator;
import static de.npcomplete.nplisp.util.LispElf.truthy;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import de.npcomplete.nplisp.Environment;
import de.npcomplete.nplisp.Lisp;
import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.Namespace;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.function.LispFunctionFactory.Fn0;
import de.npcomplete.nplisp.function.LispFunctionFactory.Fn1;
import de.npcomplete.nplisp.function.LispFunctionFactory.Fn2;
import de.npcomplete.nplisp.function.SpecialForm;
import de.npcomplete.nplisp.function.VarArgsFunction;
import de.npcomplete.nplisp.util.LispPrinter;
import de.npcomplete.nplisp.util.LispReader;
import de.npcomplete.nplisp.util.ThreadLocalFlag;

// TODO: move all things that aren't trivial method references to separate classes in a "corelibrary" package

@SuppressWarnings("rawtypes")
public final class CoreLibrary {
	private static final Symbol NS_SYM = new Symbol("ns");

	private CoreLibrary() {
		throw new IllegalStateException("No instance allowed.");
	}

	public static final class TailCall {
		public final Object[] args;

		TailCall(Object[] args) {
			this.args = args;
		}
	}

	public static final LispFunction FN_RECUR = (VarArgsFunction) TailCall::new;

	public static final LispFunction FN_LIST = (VarArgsFunction) ArraySequence::new;

	public static final LispFunction FN_VECTOR =
			(VarArgsFunction) args -> unmodifiableList(asList(args));

	public static final LispFunction FN_HASH_SET =
			(VarArgsFunction) args -> Collections.unmodifiableSet(new HashSet<>(asList(args)));

	public static final LispFunction FN_HASH_MAP = (VarArgsFunction) args -> {
		if (args.length % 2 != 0) {
			throw new LispException("hash-map function only accepts even numbers of arguments");
		}
		Map<Object, Object> map = new HashMap<>();
		Iterator<Object> it = asList(args).iterator();
		while (it.hasNext()) {
			map.put(it.next(), it.next());
		}
		return Collections.unmodifiableMap(map);
	};

	private static Sequence seq(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Sequence) {
			Sequence s = (Sequence) o;
			return s.empty() ? null : s;
		}
		if (o instanceof Map) {
			@SuppressWarnings("unchecked")
			Set<Entry> entries = ((Map) o).entrySet();
			Iterator<Entry> it = entries.iterator();
			return it.hasNext()
					? new IteratorSequence(mapIterator(it, e -> unmodifiableList(asList(e.getKey(), e.getValue()))))
					: null;
		}
		if (o instanceof Iterable) {
			Iterator it = ((Iterable) o).iterator();
			return it.hasNext() ? new IteratorSequence(it) : null;
		}
		if (o instanceof Object[]) {
			Object[] array = (Object[]) o;
			return array.length > 0 ? new ArraySequence(array) : null;
		}
		throw new LispException("Don't know how to create sequence from" + o.getClass());
	}

	/**
	 * Returns a seq on the collection. If the collection is
	 * empty, returns nil. (seq nil) returns nil. seq also works on
	 * Maps, native Java arrays (of reference types) and any objects
	 * that implement Iterable.
	 */
	public static final LispFunction FN_SEQ = (Fn1) CoreLibrary::seq;

	/**
	 * Returns the first item in the collection. Calls seq on its
	 * argument. If coll is nil, returns nil.
	 */
	public static final LispFunction FN_FIRST = (Fn1) par1 -> {
		Sequence s = seq(par1);
		return s != null ? s.first() : null;
	};

	/**
	 * Returns a seq of the items after the first. Calls seq on its
	 * argument. If there are no more items, returns nil.
	 */
	public static final LispFunction FN_NEXT = (Fn1) par1 -> {
		Sequence s = seq(par1);
		return s != null ? s.next() : null;
	};

	/**
	 * Returns a possibly empty seq of the items after the first. Calls seq on its
	 * argument.
	 */
	public static final LispFunction FN_REST = (Fn1) par1 -> {
		Sequence s = seq(par1);
		return s != null ? s.more() : EMPTY_SEQUENCE;
	};

	/**
	 * Constructs a new sequence with the new element prepended to the given sequence
	 */
	public static final LispFunction FN_CONS = (Fn2) (par1, par2) -> new Cons(par1, seq(par2));

	// TODO: replace with interop like clojure (https://github.com/clojure/clojure/blob/clojure-1.9.0/src/clj/clojure/core.clj#L652)
	public static final LispFunction FN_APPLY = new LispFunction() {

		@Override
		public Object apply(Object par1, Object par2) {
			if (!(par1 instanceof LispFunction)) {
				throw new LispException("First parameter must be a function");
			}
			LispFunction f = (LispFunction) par1;
			return f.applyTo(seq(par2));
		}

		@Override
		public Object apply(Object par1, Object par2, Object par3) {
			if (!(par1 instanceof LispFunction)) {
				throw new LispException("First parameter must be a function");
			}
			LispFunction f = (LispFunction) par1;
			return f.applyTo(new Cons(par2, seq(par3)));
		}

		@Override
		public Object apply(Object par1, Object par2, Object par3, Object... more) {
			if (!(par1 instanceof LispFunction)) {
				throw new LispException("First parameter must be a function");
			}
			LispFunction f = (LispFunction) par1;

			Object[] args = new Object[2 + more.length - 1];
			args[0] = par2;
			args[1] = par3;
			System.arraycopy(more, 0, args, 2, more.length - 1);

			Sequence rest = seq(more[more.length - 1]);

			Cons argsSeq = new Cons(args[args.length - 1], rest);
			for (int i = args.length - 2; i >= 0; i--) {
				argsSeq = new Cons(args[i], argsSeq);
			}
			return f.applyTo(argsSeq);
		}
	};

	private static boolean allIntegers(Object[] args) {
		for (Object o : args) {
			if (!(o instanceof Long || o instanceof Integer)) {
				return false;
			}
		}
		return true;
	}

	public static final LispFunction FN_ADD = (VarArgsFunction) args -> {
		if (args.length < 2) {
			return args.length == 0 ? 0L : args[0];
		}
		if (allIntegers(args)) {
			long result = ((Number) args[0]).longValue();
			for (int i = 1, length = args.length; i < length; i++) {
				result += ((Number) args[i]).longValue();
			}
			return result;
		}
		double result = ((Number) args[0]).doubleValue();
		for (int i = 1, length = args.length; i < length; i++) {
			result += ((Number) args[i]).doubleValue();
		}
		return result;
	};

	public static final LispFunction FN_SUBTRACT = (VarArgsFunction) args -> {
		if (args.length < 2) {
			if (args.length == 0) {
				throw new LispException("Wrong arity: 0");
			}
			Number n = (Number) args[0];
			if (!(n instanceof Long || n instanceof Integer)) {
				return -n.doubleValue();
			}
			return -n.longValue();
		}
		if (allIntegers(args)) {
			long result = ((Number) args[0]).longValue();
			for (int i = 1, length = args.length; i < length; i++) {
				result -= ((Number) args[i]).longValue();
			}
			return result;
		}
		double result = ((Number) args[0]).doubleValue();
		for (int i = 1, length = args.length; i < length; i++) {
			result -= ((Number) args[i]).doubleValue();
		}
		return result;
	};

	public static final LispFunction FN_MULTIPLY = (VarArgsFunction) args -> {
		if (args.length < 2) {
			return args.length == 0 ? 1L : args[0];
		}
		if (allIntegers(args)) {
			long result = ((Number) args[0]).longValue();
			for (int i = 1, length = args.length; i < length; i++) {
				result *= ((Number) args[i]).longValue();
			}
			return result;
		}
		double result = ((Number) args[0]).doubleValue();
		for (int i = 1, length = args.length; i < length; i++) {
			result *= ((Number) args[i]).doubleValue();
		}
		return result;
	};

	public static final LispFunction FN_DIVIDE = (VarArgsFunction) args -> {
		if (args.length < 2) {
			if (args.length == 0) {
				throw new LispException("Wrong arity: 0");
			}
			return 1.0 / ((Number) args[0]).doubleValue();
		}
		double result = ((Number) args[0]).doubleValue();
		for (int i = 1, length = args.length; i < length; i++) {
			double div = ((Number) args[i]).doubleValue();
			if (div == 0) {
				throw new ArithmeticException("Divide by zero");
			}
			result /= div;
		}

		if (Math.floor(result) == result && !Double.isInfinite(result) && allIntegers(args)) {
			return (long) result;
		}
		return result;
	};

	public static final LispFunction FN_STR = (VarArgsFunction) args -> {
		StringBuilder sb = new StringBuilder();
		for (Object o : args) {
			if (o != null) {
				sb.append(o);
			}
		}
		return sb.toString();
	};

	public static final LispFunction FN_NAME = (Fn1) par1 -> {
		if (par1 instanceof String) {
			return par1;
		}
		if (par1 instanceof Symbol) {
			return ((Symbol) par1).name;
		}
		if (par1 instanceof Keyword) {
			return ((Keyword) par1).name;
		}
		throw new LispException("Doesn't support name: " + FN_STR.apply(par1));
	};

	public static final LispFunction FN_SYMBOL = (Fn1) par1 -> {
		if (par1 instanceof Symbol) {
			return par1;
		}
		if (par1 instanceof Keyword) {
			Keyword kw = (Keyword) par1;
			return new Symbol(kw.nsName, kw.name);
		}
		if (par1 instanceof String) {
			return new Symbol((String) par1);
		}
		throw new LispException("Can't create symbol from: " + FN_STR.apply(par1));
	};

	public static final LispFunction FN_KEYWORD = (Fn1) par1 -> {
		if (par1 instanceof Keyword) {
			return par1;
		}
		if (par1 instanceof Symbol) {
			Symbol sym = (Symbol) par1;
			return new Keyword(sym.nsName, sym.name);
		}
		if (par1 instanceof String) {
			return new Keyword((String) par1);
		}
		throw new LispException("Can't create keyword from: " + FN_STR.apply(par1));
	};

	// BASIC I/O

	// taken from clojure.java.io/escaped-utf8-urlstring->str
	private static String escaped_utf8_urlstring_to_string(String s) {
		try {
			String encodedPlus = URLEncoder.encode("+", "UTF-8");
			String prepared = s.replace("+", encodedPlus);
			return URLDecoder.decode(prepared, "UTF-8");
		} catch (UnsupportedEncodingException ignore) {
			throw new Error("No UTF-8 available? WTF?! Impossible!");
		}
	}

	/**
	 * Coerces its argument to a {@link File}
	 */
	public static final LispFunction FN_AS_FILE = (Fn1) CoreLibrary::asFile;

	private static File asFile(Object par) {
		if (par == null) {
			return null;
		}
		if (par instanceof String) {
			return new File((String) par);
		}
		if (par instanceof File) {
			return (File) par;
		}
		if (par instanceof Path) {
			return ((Path) par).toFile();
		}
		if (par instanceof URL) {
			URL url = (URL) par;
			if ("file".equals(url.getProtocol())) {
				String path = url.getFile().replace('/', File.separatorChar);
				return new File(escaped_utf8_urlstring_to_string(path));
			} else {
				throw new LispException("Can't create File. URL is not a file: " + url);
			}
		}
		if (par instanceof URI) {
			return asFile(asUrl(par));
		}
		throw new LispException("Don't know how to create file from: " + par);
	}

	/**
	 * Coerces its argument to a {@link URL}
	 */
	public static final LispFunction FN_AS_URL = (Fn1) CoreLibrary::asUrl;

	private static URL asUrl(Object par) {
		if (par == null) {
			return null;
		}
		if (par instanceof String) {
			try {
				return new URL((String) par);
			} catch (MalformedURLException e) {
				throw new LispException("Can't create URL from String: " + par, e);
			}
		}
		if (par instanceof File) {
			try {
				return ((File) par).toURI().toURL();
			} catch (MalformedURLException e) {
				throw new LispException("Can't create URL from File: " + par, e);
			}
		}
		if (par instanceof Path) {
			try {
				return ((Path) par).toUri().toURL();
			} catch (MalformedURLException e) {
				throw new LispException("Can't create URL from Path: " + par, e);
			}
		}
		if (par instanceof URL) {
			return (URL) par;
		}
		if (par instanceof URI) {
			try {
				return ((URI) par).toURL();
			} catch (MalformedURLException e) {
				throw new LispException("Can't create URL from URI: " + par, e);
			}
		}
		throw new LispException("Don't know how to create file from: " + par);
	}

	public static final LispFunction FN_RESOURCE_URL = new LispFunction() {
		@Override
		public Object apply(Object name) {
			return apply(Lisp.class, name);
		}

		@Override
		public Object apply(Object clazz, Object name) {
			return ((Class<?>) clazz).getResource((String) name);
		}
	};

	/**
	 * Coerces its argument to a {@link Reader}.
	 * If the argument i
	 */
	public static final LispFunction FN_READER = (Fn1) par -> {
		Object source;
		try {
			source = asUrl(par);
		} catch (Exception e) {
			source = asFile(par);
		}
		try {
			return new InputStreamReader(asUrl(source).openStream());
		} catch (IOException e) {
			throw new LispException("Can't create reader from URL: " + par, e);
		}
	};

	// PRINTING //

	private static void print(Object[] args, Appendable out, BiConsumer<Object, Appendable> printFunction) {
		if (args.length > 0) {
			printFunction.accept(args[0], out);
			for (int i = 1, length = args.length; i < length; i++) {
				LispPrinter.print(" ", out);
				printFunction.accept(args[i], out);
			}
		}
	}

	public static final LispFunction FN_PR = (VarArgsFunction) args -> {
		print(args, System.out, LispPrinter::pr);
		return null;
	};

	public static final LispFunction FN_PRN = (VarArgsFunction) args -> {
		print(args, System.out, LispPrinter::pr);
		System.out.print('\n');
		return null;
	};

	public static final LispFunction FN_PR_STR = (VarArgsFunction) args -> {
		StringBuilder sb = new StringBuilder();
		print(args, sb, LispPrinter::pr);
		return sb.toString();
	};

	public static final LispFunction FN_PRN_STR = (VarArgsFunction) args -> {
		StringBuilder sb = new StringBuilder();
		print(args, sb, LispPrinter::pr);
		sb.append('\n');
		return sb.toString();
	};

	public static final LispFunction FN_PRINT = (VarArgsFunction) args -> {
		print(args, System.out, LispPrinter::print);
		return null;
	};

	public static final LispFunction FN_PRINTLN = (VarArgsFunction) args -> {
		print(args, System.out, LispPrinter::print);
		System.out.print('\n');
		return null;
	};

	public static final LispFunction FN_PRINT_STR = (VarArgsFunction) args -> {
		StringBuilder sb = new StringBuilder();
		print(args, sb, LispPrinter::print);
		return sb.toString();
	};

	public static final LispFunction FN_PRINTLN_STR = (VarArgsFunction) args -> {
		StringBuilder sb = new StringBuilder();
		print(args, sb, LispPrinter::print);
		sb.append('\n');
		return sb.toString();
	};

	// READING //

	public static final LispFunction FN_READ_STRING = (Fn1) par -> LispReader.readStr((String) par);

	public static final LispFunction FN_READ = (Fn1) par -> LispReader.read((Reader) par);

	//

	public static final SpecialForm SF_DELAY = (body, env, allowRecur) -> {
		Sequence fn_definition = new Cons(Collections.EMPTY_LIST, body);
		LispFunction f = SpecialForm.FN(fn_definition, env, false);
		return new Delay(f);
	};

	public static final LispFunction FN_DEREF = (Fn1) par -> ((Deref) par).deref();

	//

	public static final SpecialForm SF_TIME = (args, env, allowRecur) -> {
		if (args.empty() || args.next() != null) {
			throw new LispException("'time' requires exactly one argument");
		}
		long start = System.nanoTime();
		Object val = Lisp.eval(args.first(), env, allowRecur);
		long time = System.nanoTime() - start;
		FN_PRN.apply("Elapsed time: " + time / 1_000_000.0 + " msecs");
		return val;
	};

	/**
	 * Executes the body in the context of the given namespace.
	 * Creates the namespace if it does not yet exist.
	 * Returns the namespace.
	 * Note: The 'ns' form does not capture any bindings from a surrounding environment.
	 */
	public static SpecialForm SF_NS(Function<String, Namespace> internNamespace) {
		return (args, env, allowRecur) -> {
			if (args.empty()) {
				throw new LispException("'ns' requires at least one argument: (ns NAME *&BODY*)");
			}
			Object o = args.first();
			if (!isSimpleSymbol(o)) {
				throw new LispException("First argument to 'ns' must be a simple symbol");
			}
			Namespace ns = internNamespace.apply(((Symbol) o).name);

			// evaluate body
			SpecialForm.DO(args.next(), new Environment(ns), false);
			return ns;
		};
	}

	private static Object findNamespaceSource(File libFolder, String nsName) {
		String path = '/'
				+ nsName.replace('.', '/').replace('-', '_')
				+ ".edn";
		File nsFile = new File(libFolder, path);
		if (libFolder != null && nsFile.isFile()) {
			return nsFile;
		}
		return FN_RESOURCE_URL.apply(path);
	}

	/**
	 * Loads the namespace specified by the given parameter. If the parameter is a URL or File,
	 * tries to read a form from it. If the parameter is a Symbol, first attempts to resolve
	 * its name as a file in libFolder, otherwise a resource in the current classpath.
	 */
	private static Object loadNsForm(Symbol desiredNsSym, File libFolder, boolean errorOnMissingSource) {
		Object source;
		if (desiredNsSym.nsName != null) {
			throw new LispException("Can't use qualified symbol as namespace name");
		}
		source = findNamespaceSource(libFolder, desiredNsSym.name);
		if (source == null) {
			if (errorOnMissingSource) {
				throw new LispException("Can't find source for namespace: " + desiredNsSym);
			}
			return null;
		}
		Object form = LispReader.read((Reader) FN_READER.apply(source));
		if (!(form instanceof Sequence && NS_SYM.equals(((Sequence) form).first()))) {
			throw new LispException("Form read from " + source + " is not an 'ns' form");
		}
		Object nsNameSym = ((Sequence) form).more().first();
		if (!(nsNameSym instanceof Symbol) || !nsNameSym.equals(desiredNsSym)) {
			throw new LispException(
					"Namespace " + nsNameSym + " in file (" + source + ")" +
							" does not match the desired namespace " + desiredNsSym);
		}
		return form;
	}

	private static final Keyword KW_AS = new Keyword("as");
	private static final Keyword KW_REFER = new Keyword("refer");
	private static final Keyword KW_ALL = new Keyword("all");
	private static final Keyword KW_RELOAD = new Keyword("reload");
	private static final Keyword KW_RELOAD_ALL = new Keyword("reload-all");

	// TODO implement via interop using '*ns*' once possible
	public static SpecialForm SF_REQUIRE(File libFolder, Function<String, Namespace> getExistingNs) {
		ThreadLocalFlag reloadAllFlag = new ThreadLocalFlag();

		return (args, env, allowRecur) -> {
			Namespace currentNs = env.namespace;

			// TODO: add current namespace to a ThreadLocal "require chain" to avoid infinite require loops
			//       (these should not happen and are technically fine, unless :reload or :reload-all is involved)

			boolean outerReloadAll = reloadAllFlag.isSet();

			for (Object arg : args) {
				// Since 'require' is supposed to be a regular function later on, I evaluate ever
				// argument as long as this is a SpecialForm.
				// Means the vectors passed to 'require' already need to be quoted.
				arg = Lisp.eval(arg, env, false);

				Sequence spec = seq(arg);
				Object sym = spec.first();
				if (!isSimpleSymbol(sym)) {
					throw new LispException("First element of a require spec must be a simple symbol. Was: " + sym);
				}
				Symbol nsSym = (Symbol) sym;

				// load options map (:refer, :as, :reload, :reload-all)
				Map<?, ?> options = (Map<?, ?>) FN_HASH_MAP.applyTo(spec.next());

				boolean reload = truthy(options.get(KW_RELOAD));
				boolean reloadAll = truthy(options.get(KW_RELOAD_ALL));

				Namespace ns = getExistingNs.apply(nsSym.name);

				if (ns == null || reload || reloadAll || outerReloadAll) {
					Object form = loadNsForm(nsSym, libFolder, ns == null);
					if (form != null) {
						boolean startReloadAll = reloadAll && !outerReloadAll;
						reloadAllFlag.setIf(startReloadAll);
						ns = (Namespace) Lisp.eval(form, new Environment(currentNs), false);
						reloadAllFlag.unsetIf(startReloadAll);
					}
				}
				assert ns != null;

				Object alias = options.get(KW_AS);
				if (alias != null) {
					if (!isSimpleSymbol(alias)) {
						throw new LispException("Alias for namespace must be a simple symbol. Was: " + alias);
					}
					currentNs.addAlias(((Symbol) alias).name, ns);
				}
				currentNs.addAlias(ns.name, ns);

				Object refer = options.get(KW_REFER);
				if (refer != null) {
					if (refer.equals(KW_ALL)) {
						currentNs.referFrom(ns, null);
					} else {
						Sequence refSymbols  = seq(refer);
						currentNs.referFrom(ns, refSymbols != null ? refSymbols : EMPTY_SEQUENCE);
					}
				}
			}

			return null;
		};
	}

	public static final Delay CORE_NS_FORM =
			new Delay((Fn0) () -> loadNsForm(new Symbol("nplisp.core"), null, true));
}
