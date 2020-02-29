package de.npcomplete.nplisp.core;

import static de.npcomplete.nplisp.util.LispElf.isSimpleSymbol;
import static de.npcomplete.nplisp.util.LispElf.sneakyThrow;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.data.Sequence;
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
	private final Function<String, Namespace> lookupNamespace;

	public Namespace(String name, Namespace core,
			Function<Symbol, Var> internQualifiedVar,
			Function<String, Namespace> lookupNamespace) {
		referredCore = core != null ? core.mappings : null;
		this.name = name;
		this.internVar = sym -> internQualifiedVar.apply(new Symbol(name, sym.name));
		this.lookupNamespace = lookupNamespace;
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

	public Var define(Symbol symbol) {
		if (symbol.nsName != null) {
			throw new LispException("Can't def fully qualified symbols");
		}
		return mappings.computeIfAbsent(symbol, internVar);
	}

	public void addAlias(String name, Namespace ns) {
		Namespace existingAlias = aliases.get(name);
		if (existingAlias != null && !existingAlias.equals(ns)) {
			throw new LispException("Can't put a different Namespace for an existing alias");
		}
		aliases.put(name, ns);
	}

	public void referFrom(Namespace other, Sequence symbols) {
		if (symbols == null) {
			// referring all public vars
			other.mappings.forEach((sym, var) -> {
				if (!var.isPrivate()) {
					referAs(sym, var);
				}
			});
			return;
		}
		for (Object o : symbols) {
			if (!isSimpleSymbol(o)) {
				throw new LispException("Can only refer from simple symbols: " + o);
			}
			Symbol sym = (Symbol) o;
			Var var = other.mappings.get(sym);
			if (var == null) {
				throw new LispException("Can't refer var. Does not exist: " + new Var(new Symbol(other.name, sym.name)));
			}
			referAs(sym, var);
		}
	}

	private Var referAs(Symbol sym, Var v) {
		if (v.isPrivate()) {
			throw new LispException("Can't refer to private var: " + v);
		}
		referred.put(sym, v);
		return v;
	}

	public Var importAs(Symbol sym, Class<?> c) {
		return referred.computeIfAbsent(sym, internVar).bind(c);
	}

	public Var lookupVar(Symbol symbol, boolean allowPrivate, boolean nullOnUnbound) {
		Var var;
		String symNs = symbol.nsName;
		if (symNs == null) {
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
			if (var != null && !var.isPrivate()) {
				return var;
			}
		}
		String symName = symbol.name;
		if (symNs == null) {
			if (symName.startsWith("^")) {
				String classname = symName.substring(1);
				try {
					return importAs(symbol, Class.forName(classname));
				} catch (ClassNotFoundException e) {
					throw sneakyThrow(e);
				}
			}
			if (nullOnUnbound) {
				return null;
			}
			throw new LispException("Unable to resolve var '" + symName + "' in namespace '" + this.name + "'");
		}
		Namespace ns = aliases.computeIfAbsent(symNs, lookupNamespace);
		if (ns == null) {
			throw new LispException("No such namespace: '" + symNs + "'");
		}
		var = ns.lookupVar(new Symbol(symName), false, false);
		return allowPrivate
				? var
				: referAs(symbol, var);
	}
}
