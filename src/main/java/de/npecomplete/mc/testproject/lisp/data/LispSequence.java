package de.npecomplete.mc.testproject.lisp.data;

public interface LispSequence {
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
}
