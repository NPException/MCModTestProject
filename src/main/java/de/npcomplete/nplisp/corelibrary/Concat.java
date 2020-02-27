package de.npcomplete.nplisp.corelibrary;

import static de.npcomplete.nplisp.data.Sequence.EMPTY_SEQUENCE;

import de.npcomplete.nplisp.data.Sequence;

public final class Concat {
	private Concat() {
	}

	public static Sequence concat(Object arg1, Object arg2) {
		Sequence a = CoreLibrary.seq(arg1);
		Sequence b = CoreLibrary.seq(arg2);
		if (a == null) {
			return b != null ? b : EMPTY_SEQUENCE;
		}
		if (b == null) {
			return a;
		}
		return new ConcatSequence(a, b);
	}

	private static final class ConcatSequence implements Sequence {
		private final Sequence a_next;
		private Sequence b;

		private final Object first;
		private Sequence next;

		// a and b will always be non-empty here
		ConcatSequence(Sequence a, Sequence b) {
			first = a.first();
			a_next = a.next();
			this.b = b;
		}

		@Override
		public Object first() {
			return first;
		}

		@Override
		public Sequence next() {
			if (next == null) {
				next = a_next != null
						? new ConcatSequence(a_next, b)
						: b;
				b = null;
			}
			return next;
		}

		@Override
		public boolean empty() {
			return false;
		}
	}
}
