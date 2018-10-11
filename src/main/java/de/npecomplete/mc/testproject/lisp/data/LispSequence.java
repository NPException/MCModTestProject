package de.npecomplete.mc.testproject.lisp.data;

import java.util.Iterator;
import java.util.NoSuchElementException;

public interface LispSequence extends Iterable<Object> {
	//@formatter:off
	LispSequence EMPTY_SEQUENCE = new LispSequence() {
		@Override public Object first() { return null; }
		@Override public LispSequence next() { return null; }
		@Override public boolean empty() { return true; }
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
	LispSequence next();

	/**
	 * @return the next part of the sequence,
	 * or an empty sequence if there are no more elements.
	 */
	default LispSequence more() {
		LispSequence next = next();
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
		LispSequence[] box = {this};
		return new Iterator<Object>() {
			@Override
			public boolean hasNext() {
				LispSequence seq = box[0];
				return seq != null && !seq.empty();
			}

			@Override
			public Object next() {
				LispSequence seq = box[0];
				if (seq == null || empty()) {
					throw new NoSuchElementException();
				}
				box[0] = seq.next();
				return seq.first();
			}
		};
	}
}
