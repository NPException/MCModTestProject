package de.npecomplete.mc.testproject.lisp.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.LispSequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;

public class LispPrinter {

	public static String printStr(Object o) {
		StringBuilder sb = new StringBuilder();
		print(o, sb);
		return sb.toString();
	}

	public static void print(Object o, Appendable out) {
		try {
			if (o instanceof String) {
				out.append('"').append(escapeString((String) o)).append('"');
				return;
			}
			if (o instanceof Symbol) {
				out.append(((Symbol) o).name);
				return;
			}
			if (o instanceof LispSequence) {
				out.append('(');
				printIterable((Iterable) o, out);
				out.append(')');
				return;
			}
			if (o instanceof List) {
				out.append('[');
				printIterable((Iterable) o, out);
				out.append(']');
				return;
			}
			out.append(String.valueOf(o));
		} catch (IOException e) {
			throw new LispException("IO Exception when trying to print", e);
		}
	}

	private static void printIterable(Iterable<?> iterable, Appendable out) throws IOException {
		Iterator it = iterable.iterator();
		if (!it.hasNext()) {
			return;
		}
		print(it.next(), out);
		while(it.hasNext()) {
			out.append(' ');
			print(it.next(), out);
		}
	}

	// TODO: make this less horribly slow...
	private static String escapeString(String s) {
		return s.replace("\\", "\\\\")
				.replace("\b", "\\b")
				.replace("\n", "\\n")
				.replace("\t", "\\t")
				.replace("\f", "\\f")
				.replace("\r", "\\r")
				.replace("\"", "\\\"");
	}
}
