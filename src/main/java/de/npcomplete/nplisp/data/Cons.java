package de.npcomplete.nplisp.data;

import static de.npcomplete.nplisp.util.LispElf.seqEquals;
import static de.npcomplete.nplisp.util.LispElf.seqHash;

import de.npcomplete.nplisp.util.LispPrinter;

public class Cons implements Sequence {

	private final Object first;
	private final Sequence more;

	private int hash;

	public Cons(Object first, Sequence more) {
		this.first = first;
		this.more = more;
	}

	@Override
	public Object first() {
		return first;
	}

	@Override
	public Sequence next() {
		return more == null || more.empty()
				? null
				: more;
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
