package de.npcomplete.nplisp.data;

import java.util.Map;

import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.util.LispPrinter;

public final class Symbol implements LispFunction {
	public final String name;

	public Symbol(String name) {
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
		if (!(o instanceof Symbol)) {
			return false;
		}
		return name.equals(((Symbol) o).name);
	}

	@Override
	public int hashCode() {
		return 31 * Symbol.class.hashCode() + name.hashCode();
	}

	@Override
	public String toString() {
		return LispPrinter.prStr(this);
	}
}
