package de.npcomplete.nplisp;

import java.util.HashMap;
import java.util.Map;

import de.npcomplete.nplisp.data.Symbol;

public class Environment {
	private static final Object NULL_MARKER = new Object();

	private final Map<String, Object> map = new HashMap<>();
	private final Environment parent;

	public Environment(Environment parent) {
		this.parent = parent;
	}

	public Environment top() {
		Environment top = this;
		while (top.parent != null) {
			top = top.parent;
		}
		return top;
	}

	public Object lookup(Symbol symbol) throws LispException {
		Object val = map.get(symbol.name);
		if (val != null) {
			return val == NULL_MARKER ? null : val;
		}
		if (parent != null) {
			return parent.lookup(symbol);
		}
		throw new LispException("Symbol '" + symbol.name + "' is unbound");
	}

	public void bind(Symbol symbol, Object value) {
		map.put(symbol.name, value == null ? NULL_MARKER : value);
	}
}
