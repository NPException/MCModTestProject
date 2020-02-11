package de.npcomplete.nplisp.data;

import java.util.Map;
import java.util.Objects;

import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.util.LispPrinter;

public final class Keyword implements LispFunction {
	private int hash = 0;

	public final String nsName;
	public final String name;

	public Keyword(String name) {
		this(null, name);
	}

	public Keyword(String nsName, String name) {
		this.nsName = nsName;
		this.name = name;
	}

	@Override
	public Object apply(Object par) {
		return par instanceof Map
				? ((Map) par).get(this)
				: null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object apply(Object par, Object defaultValue) {
		return par instanceof Map
				? ((Map) par).getOrDefault(this, defaultValue)
				: defaultValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Keyword)) {
			return false;
		}
		Keyword other = (Keyword) o;
		return Objects.equals(nsName, other.nsName)
				&& name.equals(other.name);
	}

	@Override
	public int hashCode() {
		int h = hash;
		return h == 0
				? hash = Objects.hash(nsName, name)
				: h;
	}

	@Override
	public String toString() {
		return LispPrinter.prStr(this);
	}
}
