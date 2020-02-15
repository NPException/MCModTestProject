package de.npcomplete.nplisp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.npcomplete.nplisp.data.Symbol;

public class Environment {
	private static final Object NULL_MARKER = new Object();

	private final Map<String, Object> bindings = new HashMap<>();
	private final Environment parent;
	// the Environment was created in the context of this
	public final Namespace namespace;

	public Environment(Environment parent) {
		this.namespace = parent.namespace;
		this.parent = parent;
	}

	public Environment(Namespace namespace) {
		this.namespace = Objects.requireNonNull(namespace);
		this.parent = null;
	}

	public Object lookup(Symbol symbol) {
		if (symbol.nsName == null) {
			Object val = bindings.get(symbol.name);
			if (val != null) {
				return val == NULL_MARKER ? null : val;
			}
			if (parent != null) {
				return parent.lookup(symbol);
			}
		}
		return namespace.lookupVar(symbol).deref();
	}

	public void bind(Symbol symbol, Object value) {
		bindings.put(symbol.name, value == null ? NULL_MARKER : value);
	}
}
