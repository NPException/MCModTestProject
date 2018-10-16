package de.npecomplete.mc.testproject.lisp.data;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

public final class ListSequence implements Sequence {
	private final List<?> backingList;
	private final int index;
	private final boolean empty;

	private Sequence rest;

	/**
	 * Creates a new sequence from a given list, starting at the
	 * given index. The list is assumed to be immutable.
	 */
	public ListSequence(List<?> backingList, int index) {
		if (backingList == null) {
			throw new IllegalArgumentException("backingList must not be null");
		}
		if (index < 0) {
			throw new IllegalArgumentException("index must be >= 0");
		}
		this.backingList = backingList;
		this.index = index;
		empty = index >= backingList.size();
	}

	@Override
	public Object first() {
		return empty ? null : backingList.get(index);
	}

	@Override
	public Sequence next() {
		if (rest == null) {
			rest = index < backingList.size() - 1
					? new ListSequence(backingList, index + 1)
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
		return (Iterator<Object>) backingList.listIterator(index);
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
}
