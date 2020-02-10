package de.npcomplete.nplisp.data;

import static de.npcomplete.nplisp.util.LispElf.mapIterator;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import de.npcomplete.nplisp.Lisp;
import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.function.VarArgsFunction;
import de.npcomplete.nplisp.function.SpecialForm;
import de.npcomplete.nplisp.util.LispPrinter;

public final class CoreLibrary {
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

	public static final LispFunction FN_LIST = (VarArgsFunction) ListSequence::new;

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
			return array.length > 0 ? new ListSequence(array) : null;
		}
		throw new LispException("Don't know how to create sequence from" + o.getClass());
	}

	/**
	 * Returns a seq on the collection. If the collection is
	 * empty, returns nil. (seq nil) returns nil. seq also works on
	 * Maps, native Java arrays (of reference types) and any objects
	 * that implement Iterable.
	 */
	public static final LispFunction FN_SEQ = new LispFunction() {
		@Override
		public Object apply(Object par1) {
			return seq(par1);
		}
	};

	/**
	 * Returns the first item in the collection. Calls seq on its
	 * argument. If coll is nil, returns nil.
	 */
	public static final LispFunction FN_FIRST = new LispFunction() {
		@Override
		public Object apply(Object par1) {
			Sequence s = seq(par1);
			return s != null ? s.first() : null;
		}
	};

	/**
	 * Returns a seq of the items after the first. Calls seq on its
	 * argument. If there are no more items, returns nil.
	 */
	public static final LispFunction FN_NEXT = new LispFunction() {
		@Override
		public Object apply(Object par1) {
			Sequence s = seq(par1);
			return s != null ? s.next() : null;
		}
	};

	/**
	 * Returns a possibly empty seq of the items after the first. Calls seq on its
	 * argument.
	 */
	public static final LispFunction FN_REST = new LispFunction() {
		@Override
		public Object apply(Object par1) {
			Sequence s = seq(par1);
			return s != null ? s.more() : Sequence.EMPTY_SEQUENCE;
		}
	};

	/**
	 * Constructs a new sequence with the new element prepended to the given sequence
	 */
	public static final LispFunction FN_CONS = new LispFunction() {
		@Override
		public Object apply(Object par1, Object par2) {
			return new Cons(par1, seq(par2));
		}
	};

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

	public static final LispFunction FN_NAME = new LispFunction() {
		@Override
		public Object apply(Object arg) {
			if (arg instanceof String) {
				return arg;
			}
			if (arg instanceof Symbol) {
				return ((Symbol) arg).name;
			}
			if (arg instanceof Keyword) {
				return ((Keyword) arg).name;
			}
			throw new LispException("Doesn't support name: " + FN_STR.apply(arg));
		}
	};

	public static final LispFunction FN_SYMBOL = new LispFunction() {
		@Override
		public Object apply(Object arg) {
			if (arg instanceof Symbol) {
				return arg;
			}
			if (arg instanceof Keyword) {
				Keyword kw = (Keyword) arg;
				return new Symbol(kw.namespaceName, kw.name);
			}
			if (arg instanceof String) {
				return new Symbol((String) arg);
			}
			throw new LispException("Can't create symbol from: " + FN_STR.apply(arg));
		}
	};

	public static final LispFunction FN_KEYWORD = new LispFunction() {
		@Override
		public Object apply(Object arg) {
			if (arg instanceof Keyword) {
				return arg;
			}
			if (arg instanceof Symbol) {
				Symbol sym = (Symbol) arg;
				return new Keyword(sym.namespaceName, sym.name);
			}
			if (arg instanceof String) {
				return new Keyword((String) arg);
			}
			throw new LispException("Can't create keyword from: " + FN_STR.apply(arg));
		}
	};

	private static void print(Object[] args, Appendable out, BiConsumer<Object, Appendable> printFunction) {
		if (args.length > 0) {
			printFunction.accept(args[0], out);
			for (int i = 1, length = args.length; i < length; i++) {
				LispPrinter.print(" ", out);
				printFunction.accept(args[i], out);
			}
		}
	}

	public static final VarArgsFunction FN_PR = args -> {
		print(args, System.out, LispPrinter::pr);
		return null;
	};

	public static final LispFunction FN_PRN = (VarArgsFunction) args -> {
		print(args, System.out, LispPrinter::pr);
		System.out.print('\n');
		return null;
	};

	public static final VarArgsFunction FN_PR_STR = args -> {
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

	public static final VarArgsFunction FN_PRINT = args -> {
		print(args, System.out, LispPrinter::print);
		return null;
	};

	public static final LispFunction FN_PRINTLN = (VarArgsFunction) args -> {
		print(args, System.out, LispPrinter::print);
		System.out.print('\n');
		return null;
	};

	public static final VarArgsFunction FN_PRINT_STR = args -> {
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

	public static final SpecialForm MACRO_TIME = (args, env, allowRecur) -> {
		if (args.empty() || args.next() != null) {
			throw new LispException("'time' requires exactly one argument");
		}
		long start = System.nanoTime();
		Object val = Lisp.eval(args.first(), env, allowRecur);
		long time = System.nanoTime() - start;
		FN_PRN.apply("Elapsed time: " + time / 1_000_000.0 + " msecs");
		return val;
	};
}
