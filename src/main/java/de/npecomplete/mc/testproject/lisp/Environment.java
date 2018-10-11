package de.npecomplete.mc.testproject.lisp;

import java.util.HashMap;
import java.util.Map;

public class Environment {
	private static final Object NULL_MARKER = new Object();

	private final Map<String, Object> map = new HashMap<>();
	private final Environment parent;

	public Environment(Environment parent) {
		super();
		this.parent = parent;
	}

	public Environment top() {
		Environment top = this;
		while (top.parent != null) {
			top = top.parent;
		}
		return top;
	}

	public Object lookup(String name) {
		Object val = map.get(name);
		if (val != null) {
			return val == NULL_MARKER ? null : val;
		}
		return parent == null ? null : parent.lookup(name);
	}

	public void bind(String name, Object value) {
		map.put(name, value == null ? NULL_MARKER : value);
	}

	public void unbind(String name) {
		map.remove(name);
	}
}
