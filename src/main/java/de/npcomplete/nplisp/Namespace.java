package de.npcomplete.nplisp;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.npcomplete.nplisp.data.Symbol;

/**
 * Namespace is more or less just a top level environment
 */
public class Namespace {
	private final Map<Symbol, Var> mappings = new HashMap<>();
	private final Map<Symbol, Var> referred = new HashMap<>();
	private final Map<String, Namespace> aliases = new HashMap<>();

	private final Map<Symbol, Var> referredCore;

	public final String name;
	private final Function<Symbol, Var> internVar;

	Namespace(String name, Namespace core, Function<Symbol, Var> internQualifiedVar) {
		referredCore = core != null ? core.mappings : null;
		this.name = name;
		this.internVar = sym -> internQualifiedVar.apply(new Symbol(name, sym.name));
		// add alias to self to resolve fully qualified symbols
		addAlias(name, this);
		if (core != null) {
			addAlias(core.name, core);
		}
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

	@Override
	public String toString() {
		return name;
	}

	// TODO: toString

	public void addAlias(String name, Namespace ns) {
		Namespace existingAlias = aliases.get(name);
		if (existingAlias != null && existingAlias.equals(ns)) {
			throw new LispException("Can't put a different Namespace for an existing alias");
		}
		aliases.put(name, ns);
	}

	public Var referAs(Symbol sym, Var v) {
		referred.put(sym, v);
		return v;
	}

	public Var lookupVar(Symbol symbol) {
		Var var;
		if (symbol.nsName == null) {
			var = mappings.get(symbol);
			if (var != null) {
				return var;
			}
		}
		var = referred.get(symbol);
		if (var != null) {
			return var;
		}
		if (referredCore != null) {
			var = referredCore.get(symbol);
			if (var != null) {
				return var;
			}
		}
		if (symbol.nsName == null) {
			throw new LispException("Symbol '" + symbol.name + "' is unbound in namespace '" + name + "'");
		}
		Namespace aliasedNamespace = aliases.get(symbol.nsName);
		if (aliasedNamespace == null) {
			throw new LispException("Unknown namespace or alias: '" + symbol.nsName + "' (Namespaces need to be required before use)");
		}
		// refer for quicker lookup next time
		return referAs(symbol, aliasedNamespace.lookupVar(new Symbol(symbol.name)));
	}

	public Var define(Symbol symbol) {
		if (symbol.nsName != null) {
			throw new LispException("Can't def fully qualified symbols");
		}
		return mappings.computeIfAbsent(symbol, internVar);
	}
}
