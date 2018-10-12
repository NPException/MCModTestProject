package de.npecomplete.mc.testproject.lisp.data;

import java.util.Iterator;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

public class ListSequence implements Sequence {
	private final List<?> backingList;
	private final int index;
	private final boolean empty;

	private Sequence rest;

	/**
	 * Creates a new sequence from a given list, starting at the
	 * given index. The list is assumed to be immutable.
	 */
	public ListSequence(List<?> backingList, int index) {
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
	public String toString() {
		return LispPrinter.printStr(this);
	}
}
