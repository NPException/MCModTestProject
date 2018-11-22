package de.npecomplete.mc.testproject.lisp.data;

import static de.npecomplete.mc.testproject.lisp.util.LispElf.mapIterator;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.function.LispFunction;

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

	@FunctionalInterface
	private interface VarArgsFunction extends LispFunction {
		Object applyVarArgs(Object[] args);

		@Override
		default Object apply() {
			return applyVarArgs(new Object[0]);
		}

		@Override
		default Object apply(Object par1) {
			return applyVarArgs(new Object[] {par1});
		}

		@Override
		default Object apply(Object par1, Object par2) {
			return applyVarArgs(new Object[] {par1, par2});
		}

		@Override
		default Object apply(Object par1, Object par2, Object par3) {
			return applyVarArgs(new Object[] {par1, par2, par3});
		}

		@Override
		default Object apply(Object par1, Object par2, Object par3, Object par4, Object... more) {
			int moreCount = more.length;
			Object[] args = new Object[] {par1, par2, par3, par4};
			if (moreCount > 0) {
				args = Arrays.copyOf(args, 4 + moreCount);
				System.arraycopy(more, 0, args, 4, moreCount);
			}
			return applyVarArgs(args);
		}
	}
}
