package de.npecomplete.mc.testproject.lisp.data;

import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

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
		return name.hashCode();
	}

	@Override
	public String toString() {
		return LispPrinter.printStr(this);
	}
}
