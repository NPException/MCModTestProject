package de.npcomplete.nplisp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.npcomplete.nplisp.data.Symbol;
import de.npcomplete.nplisp.data.Var;

public class Environment {
	public static final Symbol SYM_CURRENT_NAMESPACE = new Symbol("*ns*");

	private final Map<Symbol, Var> map = new HashMap<>();
	private final Environment parent;

	public Environment(Environment parent) {
		this.parent = parent;
	}

	/**
	 * Returns the Environment which holds the current namespace var.
	 */
	public Environment currentNamespaceEnv() {
		// NOTE: at least the very top Environment must hold a current namespace.
		return map.get(SYM_CURRENT_NAMESPACE) == null
				? parent.currentNamespaceEnv()
				: this;
	}

	public Var lookup(Symbol symbol) {
		Var v = map.get(symbol);
		if (v != null) {
			return v;
		}
		if (parent != null) {
			return parent.lookup(symbol);
		}
		throw new LispException("Symbol '" + symbol + "' is unbound");
	}

	// TODO: remove. Replace all uses by bindVar
	@Deprecated
	public void bind(Symbol symbol, Object value) {
		bindVar(symbol, new Var(null, symbol.name).bindValue(value));
	}

	public void bindVar(Symbol symbol, Var v) {
		Objects.requireNonNull(v);
		map.put(symbol, v);
	}
}
