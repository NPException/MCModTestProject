package de.npcomplete.nplisp.data;

import static de.npcomplete.nplisp.util.LispElf.seqEquals;
import static de.npcomplete.nplisp.util.LispElf.seqHash;

import java.util.Iterator;
import java.util.NoSuchElementException;

import de.npcomplete.nplisp.util.LispPrinter;

public final class ArraySequence implements Sequence, Countable {
	private final Object[] array;
	private final int index;
	private final boolean empty;

	private Sequence next;

	private int hash;

	/**
	 * Creates a new sequence from a given array, starting at the
	 * given index. In an ideal world, the array should be immutable.
	 */
	private ArraySequence(Object[] array, int index) {
		this.array = array;
		this.index = index;
		empty = index >= array.length;
	}

	public ArraySequence(Object... elements) {
		if (elements == null) {
			throw new IllegalArgumentException("elements array must not be null");
		}
		this.array = elements;
		this.index = 0;
		empty = array.length == 0;
	}

	@Override
	public Object first() {
		return empty ? null : array[index];
	}

	@Override
	public Sequence next() {
		if (next == null) {
			next = index < array.length - 1
					? new ArraySequence(array, index + 1)
					: Sequence.EMPTY_SEQUENCE;
		}
		return next != Sequence.EMPTY_SEQUENCE ? next : null;
	}

	@Override
	public boolean empty() {
		return empty;
	}

	@Override
	public Iterator<Object> iterator() {
		return new ArrayIterator(index, array);
	}

	@Override
	public long count() {
		return empty ? 0 : array.length - index;
	}

	@Override
	public boolean equals(Object o) {
		return seqEquals(this, o);
	}

	@Override
	public int hashCode() {
		return hash != 0 ? hash : (hash = seqHash(this));
	}

	@Override
	public String toString() {
		return LispPrinter.prStr(this);
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
