package de.npecomplete.nplisp.data;

import de.npecomplete.nplisp.util.LispPrinter;

public final class Symbol {
	public final String name;

	public Symbol(String name) {
		this.name = name;
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
