package de.npecomplete.mc.testproject.lisp.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator.OfInt;
import java.util.stream.IntStream;

import de.npecomplete.mc.testproject.lisp.LispException;

// TODO change to Iterator<Token> and return Tokens I can easily check against

// TODO tokenize tags/set starts properly (#)

class LispTokenizer implements Iterator<String> {
	private final OfInt chars;

	private int nextChar = -1; // -1 indicates that there is no next char yet
	private String token;

	LispTokenizer(IntStream charStream) {
		chars = charStream.iterator();
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
	public String next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		String s = token;
		token = null;
		return s;
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

	private String computeNext() {
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

		// data structure begin / end
		if (isGroupToken(chr)) {
			return Character.toString((char) chr);
		}

		if (chr == '"') {
			return readStringToken();
		}

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

	private String readStringToken() {
		StringBuilder sb = new StringBuilder();
		sb.appendCodePoint('"'); // signals that this is a String to the reader
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
				return sb.toString(); // end of string
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
}
