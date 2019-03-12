package de.npcomplete.nplisp.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Namespace {
	private static final ConcurrentHashMap<String, Namespace> namespaces = new ConcurrentHashMap<>();

	public final String name;

	public final Map<Symbol, Var> bindings = new HashMap<>();

	public Namespace(String name) {
		this.name = name;
	}

	// TODO: literally everything
}
