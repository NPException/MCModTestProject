package de.npcomplete.nplisp.data;

public class Cons implements Sequence {

	private final Object first;
	private final Sequence more;

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
}
