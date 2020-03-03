package de.npcomplete.nplisp.data;

import static de.npcomplete.nplisp.util.LispElf.seqEquals;

import java.util.Iterator;
import java.util.NoSuchElementException;

public interface Sequence extends Iterable<Object> {
	//@formatter:off
	Sequence EMPTY_SEQUENCE = new Sequence() {
		@Override public Object first() { return null; }
		@Override public Sequence next() { return null; }
		@Override public boolean empty() { return true; }
		@Override public boolean equals(Object o) { return seqEquals(this, o); }
		@Override public int hashCode() { return 0; }
		@Override public String toString() { return "()"; }
	};
	//@formatter:on

	/**
	 * @return the first element of the sequence,
	 * or null if the sequence is empty.
	 */
	Object first();

	/**
	 * @return the next part of the sequence,
	 * or null if there are no more elements.
	 */
	Sequence next();

	/**
	 * @return the next part of the sequence,
	 * or an empty sequence if there are no more elements.
	 */
	default Sequence more() {
		Sequence next = next();
		return next == null ? EMPTY_SEQUENCE : next;
	}

	/**
	 * @return true if the sequence is empty.
	 */
	boolean empty();

	/**
	 * @return an iterator over the sequence
	 */
	@Override
	default Iterator<Object> iterator() {
		Sequence self = this;
		return new Iterator<Object>() {
			private Sequence seq = self;

			@Override
			public boolean hasNext() {
				return seq != null && !seq.empty();
			}

			@Override
			public Object next() {
				if (seq == null || seq.empty()) {
					throw new NoSuchElementException();
				}
				Object item = seq.first();
				seq = seq.next();
				return item;
			}
		};
	}
}
