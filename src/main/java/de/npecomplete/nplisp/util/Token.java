package de.npecomplete.nplisp.util;

class Token {
	static final Token LIST_START = new Token(Type.LIST_START, "(");
	static final Token LIST_END = new Token(Type.LIST_END, ")");
	static final Token VECTOR_START = new Token(Type.VECTOR_START, "[");
	static final Token VECTOR_END = new Token(Type.VECTOR_END, "]");
	static final Token SET_START = new Token(Type.SET_START, "#{");
	static final Token MAP_START = new Token(Type.MAP_START, "{");
	static final Token MAP_SET_END = new Token(Type.MAP_SET_END, "}");

	static final Token NIL = new Token(Type.NIL, null);

	static final Token TRUE = new Token(Type.BOOLEAN, true);
	static final Token FALSE = new Token(Type.BOOLEAN, false);

	static final Token QUOTE = new Token(Type.QUOTE, "'");

	enum Type {
		LIST_START,
		LIST_END,
		VECTOR_START,
		VECTOR_END,
		SET_START,
		MAP_START,
		MAP_SET_END,
		NIL,
		STRING,
		BOOLEAN,
		NUMBER,
		SYMBOL,
		KEYWORD,
		QUOTE,
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
