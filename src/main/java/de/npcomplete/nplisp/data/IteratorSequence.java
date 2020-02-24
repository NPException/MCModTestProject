package de.npcomplete.nplisp.data;

import java.util.Iterator;

/**
 * A sequence that is based on the given iterator. The programmer must
 * ensure that the iterator is not used by anything else but the IteratorSequence.
 * Will not retain a reference to the iterator after next() has been called.
 */
public class IteratorSequence implements Sequence {
	private static final Object NULL = new Object();
	private static final Object EMPTY = new Object();

	private Iterator<?> it;
	private Object first;
	private Sequence next;

	public IteratorSequence(Iterator<?> it) {
		this.it = it;
	}

	@Override
	public Object first() {
		if (first == null) {
			first = it.hasNext() ? it.next() : EMPTY;
			if (first == null) {
				first = NULL;
			}
		}
		return first != NULL && first != EMPTY ? first : null;
	}

	@Override
	public Sequence next() {
		if (next == null) {
			first(); // ensure iterator is called once for the first element
			next = it.hasNext()
					? new IteratorSequence(it)
					: Sequence.EMPTY_SEQUENCE;
			it = null; // allow GC to claim iterator and potentially attached collection.
		}
		return next != Sequence.EMPTY_SEQUENCE ? next : null;
	}

	@Override
	public boolean empty() {
		if (first == null) {
			first();
		}
		return first == EMPTY;
	}
}
