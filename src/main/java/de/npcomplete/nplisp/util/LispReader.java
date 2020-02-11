package de.npcomplete.nplisp.util;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.data.Keyword;
import de.npcomplete.nplisp.data.ListSequence;
import de.npcomplete.nplisp.data.Symbol;

public final class LispReader {

	public static Object readStr(String s) {
		return read(s.chars());
	}

	public static Object read(Reader reader) throws LispException {
		return read(IntStream.generate(() -> {
			try {
				return reader.read();
			} catch (IOException e) {
				throw new LispException("Failed to read from reader", e);
			}
		}));
	}

	public static Iterator<Object> readMany(Reader reader) {
		return readMany(IntStream.generate(() -> {
			try {
				return reader.read();
			} catch (IOException e) {
				throw new LispException("Failed to read from reader", e);
			}
		}));
	}

	public static Object read(IntStream chars) throws LispException {
		LispTokenizer tokenizer = new LispTokenizer(chars.iterator());
		return tokenizer.hasNext()
				? build(tokenizer, null)
				: null;
	}

	public static Iterator<Object> readMany(IntStream chars) {
		LispTokenizer tokenizer = new LispTokenizer(chars.iterator());
		return new Iterator<Object>() {
			@Override
			public boolean hasNext() {
				return tokenizer.hasNext();
			}

			@Override
			public Object next() {
				return build(tokenizer, null);
			}
		};
	}

	private static Object build(Iterator<Token> it, Token expectedEnd) {
		if (!it.hasNext()) {
			throw new LispException("Encountered end of data while reading");
		}
		Token token = it.next();

		switch (token.type) {
			case LIST_END:
			case VECTOR_END:
			case MAP_SET_END:
				if (token == expectedEnd) {
					return token;
				}
				throw new LispException("Unexpected token while reading: "
						+ token.type + " -> " + token.value);

			case LIST_START:
				ArrayList<Object> seqContent = new ArrayList<>();
				buildCollection(seqContent, Token.LIST_END, it);
				return new ListSequence(seqContent.toArray());

			case VECTOR_START:
				ArrayList<Object> list = new ArrayList<>();
				buildCollection(list, Token.VECTOR_END, it);
				list.trimToSize();
				return Collections.unmodifiableList(list);

			case SET_START:
				HashSet<Object> set = new HashSet<>();
				buildCollection(set, Token.MAP_SET_END, it);
				return Collections.unmodifiableSet(set);

			case MAP_START:
				return buildMap(it);

			case NIL:
			case STRING:
			case BOOLEAN:
			case NUMBER:
				return token.value;

			case SYMBOL:
				return new Symbol((String) token.value);

			case KEYWORD:
				return new Keyword((String) token.value);

			case QUOTE:
				Object[] quoted = {new Symbol("quote"), build(it, null)};
				return new ListSequence(quoted);

			case VAR:
				Object[] varCall = {new Symbol("var"), build(it, null)};
				return new ListSequence(varCall);

			case TAG:
				// NOT YET SUPPORTED
		}

		throw new LispException("Token type " + token.type.name()
				+ " not yet supported. Value was: " + token.value);
	}

	private static void buildCollection(Collection<Object> base, Token end, Iterator<Token> it) {
		while (it.hasNext()) {
			Object value = build(it, end);
			if (value == end) {
				return;
			}
			if (!base.add(value) && base instanceof Set) {
				throw new LispException("Duplicate key in set literal: " + value);
			}
		}
		throw new LispException("Encountered end of data while reading a collection");
	}

	private static Object buildMap(Iterator<Token> it) {
		ArrayList<Object> mapContents = new ArrayList<>();
		buildCollection(mapContents, Token.MAP_SET_END, it);
		if (mapContents.size() % 2 != 0) {
			throw new LispException("Odd number or elements for map literal");
		}
		Map<Object, Object> map = new HashMap<>(mapContents.size());
		Iterator mapIt = mapContents.iterator();
		while (mapIt.hasNext()) {
			Object key = mapIt.next();
			if (map.containsKey(key)) {
				throw new LispException("Duplicate key in map literal: " + key);
			}
			map.put(key, mapIt.next());
		}
		return Collections.unmodifiableMap(map);
	}
}