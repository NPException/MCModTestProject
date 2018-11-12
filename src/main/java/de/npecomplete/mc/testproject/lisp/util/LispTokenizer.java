package de.npecomplete.mc.testproject.lisp.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator.OfInt;

import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.util.Token.Type;

class LispTokenizer implements Iterator<Token> {
	private final OfInt chars;

	private int nextChar = -1; // -1 indicates that there is no next char yet
	private Token token;

	LispTokenizer(OfInt chars) {
		this.chars = chars;
	}

	@Override
	public boolean hasNext() {
		if (token != null) {
			return true;
		}
		token = computeNext();
		return token != null;
	}

	@Override
	public Token next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		Token t = token;
		token = null;
		return t;
	}

	private int nextChar() {
		if (nextChar != -1) {
			int chr = nextChar;
			nextChar = -1;
			return chr;
		}
		return chars.hasNext() ? chars.next() : -1;
	}

	private static boolean isIgnored(int chr) {
		return Character.isWhitespace(chr) || chr == ',';
	}

	private static boolean isGroupToken(int chr) {
		return chr == '(' || chr == ')'
				|| chr == '[' || chr == ']'
				|| chr == '{' || chr == '}';
	}

	private Token computeNext() {
		int chr = nextChar();
		if (chr == -1) {
			return null; // end of character stream
		}

		// skip whitespace and comma
		while (isIgnored(chr)) {
			if ((chr = nextChar()) == -1) {
				return null;  // end of character stream
			}
		}

		if (chr == ';') {
			chr = nextNonCommentChar();
			if (chr == -1) {
				return null; // end of character stream
			}
		}

		// data structure begin / end
		if (isGroupToken(chr)) {
			switch (chr) {
				case '(':
					return Token.SEQUENCE_START;
				case ')':
					return Token.SEQUENCE_END;
				case '[':
					return Token.LIST_START;
				case ']':
					return Token.LIST_END;
				case '{':
					return Token.MAP_START;
				case '}':
					return Token.MAP_SET_END;
			}
			throw new IllegalStateException("Unhandled group token: " + Character.toString((char) chr));
		}

		if (chr == '"') {
			return readStringToken();
		}

		if (chr == '#') {
			if ((chr = nextChar()) == -1) {
				throw new LispException("Encountered end of data when trying to read tagged element");
			}
			if (chr == '{') {
				return Token.SET_START;
			}
			return new Token(Type.TAG, finishToken(chr));
		}

		if (chr == ':') {
			if ((chr = nextChar()) == -1) {
				throw new LispException("Encountered end of data when trying to read keyword");
			}
			return new Token(Type.KEYWORD, finishToken(chr));
		}

		String value = finishToken(chr);

		// check booleans and null
		if (value.equals("true")) {
			return Token.TRUE;
		}

		if (value.equals("false")) {
			return Token.FALSE;
		}

		if (value.equals("null")) {
			return Token.NULL;
		}

		if (isNumberCandidate(value)) {
			if (value.chars().allMatch(Character::isDigit)) {
				try {
					long number = Long.parseLong(value);
					return new Token(Type.NUMBER, number);
				} catch (NumberFormatException e) {
					throw new LispException("Could not read long: " + value, e);
				}
			}
			try {
				double number = Double.parseDouble(value);
				return new Token(Type.NUMBER, number);
			} catch (NumberFormatException e) {
				throw new LispException("Could not read double: " + value, e);
			}
		}

		return new Token(Type.SYMBOL, value);
	}

	private static boolean isNumberCandidate(String value) {
		char first = value.charAt(0);
		if (Character.isDigit(first)) {
			return true;
		}
		char second = value.length() > 1 ? value.charAt(1) : '?';
		return (first == '-' || first == '+')
				&& Character.isDigit(second);
	}

	private String finishToken(int chr) {
		StringBuilder sb = new StringBuilder();
		sb.appendCodePoint(chr);
		while ((chr = nextChar()) != -1) {
			if (isIgnored(chr) || isGroupToken(chr)) {
				nextChar = chr;
				break;
			}
			sb.appendCodePoint(chr);
		}
		return sb.toString();
	}

	private Token readStringToken() {
		StringBuilder sb = new StringBuilder();
		boolean escaped = false;

		int chr;
		while ((chr = nextChar()) != -1) {
			if (escaped) {
				chr = readEscapedChar(chr);
				escaped = false;
			} else if (chr == '\\') {
				escaped = true;
				continue;
			} else if (chr == '"') {
				return new Token(Type.STRING, sb.toString()); // end of string
			}
			sb.appendCodePoint(chr);
		}
		throw new LispException("Encountered end of data while reading String");
	}

	private static int readEscapedChar(int chr) {
		// @formatter:off
		switch (chr) {
			case '\\': return '\\';
			case 'b' : return '\b';
			case 'n' : return '\n';
			case 't' : return '\t';
			case 'f' : return '\f';
			case 'r' : return '\r';
			case '"' : return '"';
		}
		// @formatter:on
		throw new LispException("Encountered unknown escaped character: \\"
				+ Character.toString((char) chr));
	}

	/**
	 * Returns the next character that is not part of a comment and not ignored,
	 * or -1 if no more chars are available.
	 */
	private int nextNonCommentChar() {
		int chr;
		boolean isComment = true;
		do {
			chr = nextChar();
			if (chr == '\n' || chr == -1) {
				isComment = false;
			} else if (chr == ';') {
				isComment = true;
			}
		} while (isComment || isIgnored(chr));
		return chr;
	}
}