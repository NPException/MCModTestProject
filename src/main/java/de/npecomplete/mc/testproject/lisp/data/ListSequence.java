package de.npecomplete.mc.testproject.lisp.data;

import java.util.List;

public class ListSequence implements LispSequence {
	private final List<?> backingList;
	private final int index;

	private LispSequence rest;

	public ListSequence(List<?> backingList, int index) {
		this.backingList = backingList;
		this.index = index;
	}

	@Override
	public Object first() {
		return empty() ? null : backingList.get(index);
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
		return index >= backingList.size();
	}
}
