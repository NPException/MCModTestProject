package de.npcomplete.nplisp.data;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

import de.npcomplete.nplisp.util.LispPrinter;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class RandomAccessListSequence implements Sequence, Countable {
	private final List list;
	private final int index;
	private final boolean empty;

	private Sequence next;

	private int hash = 0;

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
		Iterator<?> a = iterator();
		Iterator<?> b = other.iterator();
		while (a.hasNext() && b.hasNext()) {
			if (!Objects.equals(a.next(), b.next())) {
				return false;
			}
		}
		return !(a.hasNext() || b.hasNext());
	}

	@Override
	public int hashCode() {
		if (hash != 0) {
			return hash;
		}
		if (empty) {
			return 0;
		}
		int result = 1;
		for (Object element : this) {
			result = 31 * result + Objects.hashCode(element);
		}
		return hash = result;
	}

	@Override
	public String toString() {
		return LispPrinter.prStr(this);
	}
}
