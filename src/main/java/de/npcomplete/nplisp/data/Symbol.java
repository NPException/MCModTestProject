package de.npcomplete.nplisp.data;

import java.util.Map;
import java.util.Objects;

import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.util.LispPrinter;

public final class Symbol implements LispFunction {
	private int hash = 0;

	public final String nsName;
	public final String name;

	public Symbol(String name) {
		if (name.indexOf('/') == -1 || name.length() == 1) {
			this.nsName = null;
			this.name = name;
			return;
		}
		String[] nsAndName = name.split("/", 2);
		this.nsName = verifyNs(nsAndName[0]);
		this.name = verifyName(nsAndName[1]);
	}

	public Symbol(String nsName, String name) {
		this.nsName = verifyNs(nsName);
		this.name = verifyName(name);
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
		Symbol other = (Symbol) o;
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


	static String verifyNs(String nsName) {
		if (nsName.length() == 0
				|| nsName.indexOf('/') != -1) {
			throw new LispException("Invalid namespace name: '" + nsName + "'");
		}
		return nsName;
	}

	static String verifyName(String name) {
		if (name.length() == 0
				|| name.indexOf('/') != -1 && name.length() != 1) {
			throw new LispException("Invalid name: '" + name + "'");
		}
		return name;
	}
}
