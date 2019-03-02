package de.npcomplete.nplisp.data;

import java.util.Iterator;
import java.util.Objects;

import de.npcomplete.nplisp.util.LispPrinter;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Sequence)) {
			return false;
		}
		Sequence other = (Sequence) o;
		if (empty() != other.empty()) {
			return false;
		}
		if (empty() && other.empty()) {
			return true;
		}
		Iterator a = iterator();
		Iterator b = other.iterator();
		while (a.hasNext() && b.hasNext()) {
			if (!Objects.equals(a.next(), b.next())) {
				return false;
			}
		}
		return !(a.hasNext() || b.hasNext());
	}

	@Override
	public int hashCode() {
		int result = 1;
		for (Object element : this) {
			result = 31 * result + Objects.hashCode(element);
		}
		return result;
	}

	@Override
	public String toString() {
		return LispPrinter.prStr(this);
	}
}
