package de.npecomplete.mc.testproject.lisp.data;

import static de.npecomplete.mc.testproject.lisp.util.LispElf.mapIterator;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.function.LispFunction;
import de.npecomplete.mc.testproject.lisp.function.VarArgsFunction;

public final class CoreLibrary {
	private CoreLibrary() {
		throw new IllegalStateException("No instance allowed.");
	}

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
		if (o instanceof Iterable) {
			Iterator it = ((Iterable) o).iterator();
			return it.hasNext() ? new IteratorSequence(it) : null;
		}
		if (o instanceof Map) {
			@SuppressWarnings("unchecked")
			Set<Entry> entries = ((Map) o).entrySet();
			Iterator<Entry> it = entries.iterator();
			return it.hasNext()
					? new IteratorSequence(mapIterator(it, e -> unmodifiableList(asList(e.getKey(), e.getValue()))))
					: null;
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
		public Object apply(Object par1, Object par2, Object par3, Object par4, Object... more) {
			if (!(par1 instanceof LispFunction)) {
				throw new LispException("First parameter must be a function");
			}
			LispFunction f = (LispFunction) par1;
			if (more.length == 0) {
				return f.applyTo(new Cons(par2, new Cons(par3, seq(par4))));
			}

			Sequence rest = seq(more[more.length - 1]);

			Object[] args = new Object[3 + more.length - 1];
			args[0] = par2;
			args[1] = par3;
			args[2] = par4;
			System.arraycopy(more, 0, args, 3, more.length - 1);

			Cons argsSeq = new Cons(args[args.length - 1], rest);
			for (int i = args.length - 2; i>=0; i--) {
				argsSeq = new Cons(args[i], argsSeq);
			}
			return f.applyTo(argsSeq);
		}
	};

	// TODO: implement properly
	public static final LispFunction FN_PLUS = (VarArgsFunction) args -> {
		double sum = 0.0;
		for (Object o : args) {
			sum += ((Number) o).doubleValue();
		}
		return sum;
	};

}
