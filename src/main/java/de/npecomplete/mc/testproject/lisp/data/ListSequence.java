package de.npecomplete.mc.testproject.lisp.data;

import java.util.List;

public class ListSequence implements LispSequence {
	private final List<?> backingList;
	private final int index;
	private final boolean empty;

	private LispSequence rest;

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
	public LispSequence next() {
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Sequence([");
		if (!empty) {
			sb.append(first());
			LispSequence rest = next();
			while (rest != null) {
				sb.append(",").append(rest.first());
				rest = rest.next();
			}
		}
		sb.append("])");
		return sb.toString();
	}
}
