package de.npcomplete.nplisp.corelibrary;

import static de.npcomplete.nplisp.data.Sequence.EMPTY_SEQUENCE;
import static de.npcomplete.nplisp.util.LispElf.seqEquals;
import static de.npcomplete.nplisp.util.LispElf.seqHash;

import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.util.LispPrinter;

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

		private int hash;

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

		@Override
		public boolean equals(Object o) {
			return seqEquals(this, o);
		}

		@Override
		public int hashCode() {
			return hash != 0 ? hash : (hash = seqHash(this));
		}

		@Override
		public String toString() {
			return LispPrinter.prStr(this);
		}
	}
}
