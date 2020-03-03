package de.npcomplete.nplisp.data;

import static de.npcomplete.nplisp.util.LispElf.seqEquals;
import static de.npcomplete.nplisp.util.LispElf.seqHash;

import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import de.npcomplete.nplisp.util.LispPrinter;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class RandomAccessListSequence implements Sequence, Countable {
	private final List list;
	private final int index;
	private final boolean empty;

	private Sequence next;

	private int hash;

	/**
	 * Creates a new sequence from a given LList, starting at the
	 * given index. In an ideal world, the List should be immutable.
	 */
	private RandomAccessListSequence(List list, int index) {
		this.list = list;
		this.index = index;
		empty = index >= list.size();
	}

	public RandomAccessListSequence(List<?> list) {
		if (!(list instanceof RandomAccess)) {
			throw new IllegalArgumentException("list must implement RandomAccess");
		}
		this.list = list;
		this.index = 0;
		empty = list.size() == 0;
	}

	@Override
	public Object first() {
		return empty ? null : list.get(index);
	}

	@Override
	public Sequence next() {
		if (next == null) {
			next = index < list.size() - 1
					? new RandomAccessListSequence(list, index + 1)
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
		return list.listIterator(index);
	}

	@Override
	public long count() {
		return empty ? 0 : list.size() - index;
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
}
