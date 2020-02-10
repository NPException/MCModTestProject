package de.npcomplete.nplisp.data;

import java.util.Map;

import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.util.LispPrinter;

public final class Keyword implements LispFunction {
	public final String namespaceName;
	public final String name;

	public Keyword(String name) {
		this(null, name);
	}

	public Keyword(String namespaceName, String name) {
		this.namespaceName = namespaceName;
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
		return name.equals(((Keyword) o).name);
	}

	@Override
	public int hashCode() {
		return 31 * Keyword.class.hashCode() + name.hashCode();
	}

	@Override
	public String toString() {
		return LispPrinter.prStr(this);
	}
}
