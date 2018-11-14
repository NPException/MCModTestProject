package de.npecomplete.mc.testproject.lisp.data;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

public final class ListSequence implements Sequence {
	public final static ListSequence EMPTY = new ListSequence();

	private final Object[] array;
	private final int index;
	private final boolean empty;

	private Sequence rest;

	/**
	 * Creates a new list sequence from a given array, starting at the
	 * given index. In an ideal world, the array should be immutable.
	 */
	private ListSequence(Object[] array, int index) {
		if (array == null) {
			throw new IllegalArgumentException("array must not be null");
		}
		if (index < 0) {
			throw new IllegalArgumentException("index must be >= 0");
		}
		this.array = array;
		this.index = index;
		empty = index >= array.length;
	}

	public ListSequence(Object ... elements) {
		this(elements, 0);
	}

	@Override
	public Object first() {
		return empty ? null : array[index];
	}

	@Override
	public Sequence next() {
		if (rest == null) {
			rest = index < array.length - 1
					? new ListSequence(array, index + 1)
					: null;
		}
		return rest;
	}

	@Override
	public boolean empty() {
		return empty;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Object> iterator() {
		return new ArrayIterator(index, array);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Sequence)) {
			return false;
		}
		Sequence other = (Sequence) o;
		if (empty() != other.empty()) {
			return false;
		}
		if (empty() && other.empty()) {
			return true;
		}
		Iterator a = iterator();
		Iterator b = other.iterator();
		while (a.hasNext() && b.hasNext()) {
			if (!Objects.equals(a.next(), b.next())) {
				return false;
			}
		}
		return !(a.hasNext() || b.hasNext());
	}

	@Override
	public int hashCode() {
		if (empty) {
			return 0;
		}
		int result = 1;
		for (Object element : this) {
			result = 31 * result + Objects.hashCode(element);
		}
		return result;
	}

	@Override
	public String toString() {
		return LispPrinter.printStr(this);
	}

	private static class ArrayIterator implements Iterator<Object> {
		private final Object[] array;
		private int index;

		ArrayIterator(int index, Object[] array) {
			this.index = index;
			this.array = array;
		}

		@Override
		public boolean hasNext() {
			return index < array.length;
		}

		@Override
		public Object next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return array[index++];
		}
	}
}
