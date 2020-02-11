package de.npcomplete.nplisp;

import java.util.HashMap;
import java.util.Map;

import de.npcomplete.nplisp.data.Symbol;

/**
 * Namespace is more or less just a top level environment
 */
public class Namespace extends Environment {
	private final Map<Symbol, Var> mappings = new HashMap<>();
	private final Map<String, Namespace> aliases = new HashMap<>();

	public final String name;

	public Namespace(String name) {
		super(null);
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Namespace)) {
			return false;
		}
		Namespace other = (Namespace) o;
		return name.equals(other.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public void addAlias(String name, Namespace ns) {
		Namespace existingAlias = aliases.get(name);
		if (existingAlias != null && existingAlias.equals(ns)) {
			throw new LispException("Can't put a different Namespace for an existing alias");
		}
		aliases.put(name, ns);
	}

	public Var referAs(Symbol sym, Var v) {
		mappings.put(sym, v);
		return v;
	}

	public Var lookupVar(Symbol symbol) {
		Var v = mappings.get(symbol);
		if (v != null) {
			return v;
		}
		if (symbol.nsName == null) {
			throw new LispException("Symbol '" + symbol.name + "' is unbound in namespace '" + name + "'");
		}
		Namespace aliasedNamespace = aliases.get(symbol.nsName);
		if (aliasedNamespace == null) {
			throw new LispException("Namespace '" + symbol.nsName + "' needs to be required before use");
		}
		// refer for quicker lookup next time
		return referAs(symbol, aliasedNamespace.lookupVar(new Symbol(symbol.name)));
	}

	public Var def(Symbol symbol) {
		if (symbol.nsName != null) {
			throw new LispException("Can't def fully qualified symbols!");
		}
		// TODO: intern all Vars in Lisp class
		return mappings.computeIfAbsent(symbol, sym -> new Var(name, sym.name));
	}
}
