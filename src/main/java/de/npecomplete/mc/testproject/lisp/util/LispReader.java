package de.npecomplete.mc.testproject.lisp.util;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.stream.IntStream;

import de.npecomplete.mc.testproject.lisp.LispException;

public class LispReader {

	public static void main(String[] args) {
		Object o = readStr("(fn [x] (println #{\"Hello\t\tWorld!\"}))");
		System.out.println(o);
		System.out.println(LispPrinter.printStr(o));
	}

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
		Iterator<String> it = new LispTokenizer(chars);
		while (it.hasNext()) {
			String token = it.next();
			System.out.println(token);
		}
		return null;
	}
}