package de.npecomplete.mc.testproject.lisp.util;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.ArraySequence;
import de.npecomplete.mc.testproject.lisp.data.Keyword;
import de.npecomplete.mc.testproject.lisp.data.Symbol;

public class LispReader {

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

	public static Object read(IntStream chars) throws LispException {
		return build(new LispTokenizer(chars), null);
	}

	private static Object build(Iterator<Token> it, Token expectedEnd) {
		if (!it.hasNext()) {
			throw new LispException("Encountered end of data while reading");
		}
		Token token = it.next();

		switch (token.type) {
			case SEQUENCE_END:
			case LIST_END:
			case MAP_SET_END:
				if (token == expectedEnd) {
					return token;
				}
				throw new LispException("Unexpected token while reading: "
						+ token.type + " -> " + token.value);

			case SEQUENCE_START:
				ArrayList<Object> seqContent = new ArrayList<>();
				buildCollection(seqContent, Token.SEQUENCE_END, it);
				return new ArraySequence(seqContent.toArray(), 0);

			case LIST_START:
				ArrayList<Object> list = new ArrayList<>();
				buildCollection(list, Token.LIST_END, it);
				list.trimToSize();
				return list;

			case SET_START:
				HashSet<Object> set = new HashSet<>();
				buildCollection(set, Token.MAP_SET_END, it);
				return set;

			case MAP_START:
				return buildMap(it);

			case NULL:
			case STRING:
			case BOOLEAN:
			case NUMBER:
				return token.value;

			case SYMBOL:
				return new Symbol((String) token.value);

			case KEYWORD:
				return new Keyword((String) token.value);

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
			base.add(value);
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
			map.put(mapIt.next(), mapIt.next());
		}
		return Collections.unmodifiableMap(map);
	}
}