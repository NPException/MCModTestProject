package de.npcomplete.nplisp.data;

import java.util.HashMap;
import java.util.Map;

public final class Namespace {
	public final String name;

	private final Map<Symbol, Var> bindings = new HashMap<>();

	public Namespace(String name) {
		this.name = name;
	}

	public Var intern(String name) {
		return bindings.computeIfAbsent(new Symbol(name), k -> new Var(this, name));
	}

	// TODO: literally everything
}
