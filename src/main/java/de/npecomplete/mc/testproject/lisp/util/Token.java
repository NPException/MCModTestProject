package de.npecomplete.mc.testproject.lisp.util;

class Token {
	static final Token SEQUENCE_START = new Token(Type.SEQUENCE_START, "(");
	static final Token SEQUENCE_END = new Token(Type.SEQUENCE_END, ")");
	static final Token LIST_START = new Token(Type.LIST_START, "[");
	static final Token LIST_END = new Token(Type.LIST_END, "]");
	static final Token SET_START = new Token(Type.SET_START, "#{");
	static final Token MAP_START = new Token(Type.MAP_START, "{");
	static final Token MAP_SET_END = new Token(Type.MAP_SET_END, "}");

	static final Token NULL = new Token(Type.NULL, null);

	static final Token TRUE = new Token(Type.BOOLEAN, true);
	static final Token FALSE = new Token(Type.BOOLEAN, false);

	enum Type {
		SEQUENCE_START,
		SEQUENCE_END,
		LIST_START,
		LIST_END,
		SET_START,
		MAP_START,
		MAP_SET_END,
		NULL,
		STRING,
		BOOLEAN,
		NUMBER,
		SYMBOL,
		KEYWORD,
		TAG
	}

	final Type type;
	final Object value;

	Token(Type type, Object value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public String toString() {
		return "Token[" + type + ", " + value + ']';
	}
}
