package de.npecomplete.nplisp.data;

import de.npecomplete.nplisp.util.LispPrinter;

public final class Keyword {
	public final String name;

	public Keyword(String name) {
		this.name = name;
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
