package de.npecomplete.mc.testproject.lisp.data;

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
}
