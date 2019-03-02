package de.npcomplete.nplisp.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.data.Keyword;
import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.data.Symbol;

public final class LispPrinter {

	public static String prStr(Object o) throws LispException {
		StringBuilder sb = new StringBuilder();
		pr(o, sb);
		return sb.toString();
	}

	public static void pr(Object o, Appendable out) throws LispException {
		try {
			if (o == null) {
				out.append("nil");
				return;
			}
			if (o instanceof String) {
				out.append('"').append(escapeString((String) o)).append('"');
				return;
			}
			if (o instanceof Symbol) {
				out.append(((Symbol) o).name);
				return;
			}
			if (o instanceof Keyword) {
				out.append(':');
				out.append(((Keyword) o).name);
				return;
			}
			if (o instanceof Sequence) {
				out.append('(');
				Iterable<?> i = (Iterable) o;
				printIterable(out, i, " ", LispPrinter::pr);
				out.append(')');
				return;
			}
			if (o instanceof List) {
				out.append('[');
				Iterable<?> i = (Iterable) o;
				printIterable(out, i, " ", LispPrinter::pr);
				out.append(']');
				return;
			}
			if (o instanceof Set) {
				out.append("#{");
				Iterable<?> i = (Iterable) o;
				printIterable(out, i, " ", LispPrinter::pr);
				out.append('}');
				return;
			}
			if (o instanceof Map) {
				out.append('{');
				@SuppressWarnings("unchecked")
				Iterable<Entry> i = ((Map) o).entrySet();
				printIterable(out, i, ", ", (e, a) -> {
					pr(e.getKey(), a);
					a.append(' ');
					pr(e.getValue(), a);
				});
				out.append('}');
				return;
			}
			out.append(String.valueOf(o));
		} catch (IOException e) {
			throw new LispException("IO Exception when trying to print", e);
		}
	}

	public static String printStr(Object o) throws LispException {
		StringBuilder sb = new StringBuilder();
		print(o, sb);
		return sb.toString();
	}

	public static void print(Object o, Appendable out) throws LispException {
		try {
			if (o == null) {
				out.append("nil");
				return;
			}
			if (o instanceof String) {
				out.append((String) o);
				return;
			}
			if (o instanceof Symbol) {
				out.append(((Symbol) o).name);
				return;
			}
			if (o instanceof Keyword) {
				out.append(':');
				out.append(((Keyword) o).name);
				return;
			}
			if (o instanceof Sequence) {
				out.append('(');
				Iterable<?> i = (Iterable) o;
				printIterable(out, i, " ", LispPrinter::print);
				out.append(')');
				return;
			}
			if (o instanceof List) {
				out.append('[');
				Iterable<?> i = (Iterable) o;
				printIterable(out, i, " ", LispPrinter::print);
				out.append(']');
				return;
			}
			if (o instanceof Set) {
				out.append("#{");
				Iterable<?> i = (Iterable) o;
				printIterable(out, i, " ", LispPrinter::print);
				out.append('}');
				return;
			}
			if (o instanceof Map) {
				out.append('{');
				@SuppressWarnings("unchecked")
				Iterable<Entry> i = ((Map) o).entrySet();
				printIterable(out, i, ", ", (e, a) -> {
					print(e.getKey(), a);
					a.append(' ');
					print(e.getValue(), a);
				});
				out.append('}');
				return;
			}
			out.append(String.valueOf(o));
		} catch (IOException e) {
			throw new LispException("IO Exception when trying to print", e);
		}
	}

	private static <T> void printIterable(Appendable out, Iterable<T> iterable,
			String seperator, ElementPrinter<T> elementPrinter)
			throws IOException {
		Iterator<T> it = iterable.iterator();
		if (!it.hasNext()) {
			return;
		}
		elementPrinter.print(it.next(), out);
		while (it.hasNext()) {
			out.append(seperator);
			elementPrinter.print(it.next(), out);
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

	private interface ElementPrinter<T> {
		void print(T element, Appendable a) throws IOException;
	}
}
