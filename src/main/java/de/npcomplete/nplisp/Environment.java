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
	private final Namespace namespace;

	public Environment(Environment parent) {
		this(parent.namespace, parent);
	}

	public Environment(Namespace namespace, Environment parent) {
		this.namespace = Objects.requireNonNull(namespace);
		this.parent = parent;
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
		return lookupVar(symbol).deref();
	}

	public Var lookupVar(Symbol symbol) {
		return namespace.lookupVar(symbol);
	}

	public Var defineVar(Symbol symbol) {
		return namespace.defineVar(symbol);
	}

	public void bind(Symbol symbol, Object value) {
		bindings.put(symbol.name, value == null ? NULL_MARKER : value);
	}
}
