package de.npcomplete.nplisp.corelibrary;

import static de.npcomplete.nplisp.corelibrary.CoreLibrary.cons;
import static de.npcomplete.nplisp.corelibrary.CoreLibrary.count;
import static de.npcomplete.nplisp.corelibrary.CoreLibrary.isMap;
import static de.npcomplete.nplisp.corelibrary.CoreLibrary.isSeq;
import static de.npcomplete.nplisp.corelibrary.CoreLibrary.isSet;
import static de.npcomplete.nplisp.corelibrary.CoreLibrary.isVector;
import static de.npcomplete.nplisp.corelibrary.CoreLibrary.list;
import static de.npcomplete.nplisp.corelibrary.CoreLibrary.seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.core.Namespace;
import de.npcomplete.nplisp.core.Var;
import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.data.Symbol;

// TODO gensyms (see clojure.core/or)
// TODO Find a way to implement in nplisp.core
//      (reference: clojure.tools.reader/syntax-quote*)
public final class SyntaxQuote {
	private SyntaxQuote() {}

	private static final Symbol SYM_UNQUOTE = new Symbol("unquote");
	private static final Symbol SYM_UNQUOTE_SPLICING = new Symbol("unquote-splicing");

	private static final Symbol SYM_CORE_APPLY = new Symbol("nplisp.core", "apply");
	private static final Symbol SYM_CORE_CONCAT = new Symbol("nplisp.core", "concat");
	private static final Symbol SYM_CORE_QUOTE = new Symbol("nplisp.core", "quote");
	private static final Symbol SYM_CORE_SEQUENCE = new Symbol("nplisp.core", "sequence");
	private static final Symbol SYM_CORE_LIST = new Symbol("nplisp.core", "list");
	private static final Symbol SYM_CORE_VECTOR = new Symbol("nplisp.core", "vector");
	private static final Symbol SYM_CORE_HASH_SET = new Symbol("nplisp.core", "hash-set");
	private static final Symbol SYM_CORE_HASH_MAP = new Symbol("nplisp.core", "hash-map");

	private static boolean isUnquote(Object item) {
		return isSeq(item) && SYM_UNQUOTE.equals(((Sequence) item).first());
	}

	private static boolean isUnquoteSplice(Object item) {
		return isSeq(item) && SYM_UNQUOTE_SPLICING.equals(((Sequence) item).first());
	}

	private static Object unquote(Object o) {
		Sequence form = (Sequence) o;
		if (count(form) != 2) {
			throw new LispException(
					"'unquote' and 'unquote-splicing' take exactly one argument. Was " + count(form));
		}
		return form.next().first();
	}

	private static Object expandList(Iterable<?> coll, Namespace ns) {
		List<Object> expansion = new ArrayList<>();
		for (Object item : coll) {
			if (isUnquote(item)) {
				expansion.add(list(SYM_CORE_LIST, unquote(item)));
			} else if (isUnquoteSplice(item)) {
				expansion.add(unquote(item));
			} else {
				expansion.add(list(SYM_CORE_LIST, syntaxQuote(item, ns)));
			}
		}
		return seq(expansion);
	}

	private static Object syntaxQuoteColl(Symbol type, Object coll, Namespace ns) {
		Object res =
				list(SYM_CORE_SEQUENCE,
						cons(SYM_CORE_CONCAT, expandList((Iterable<?>) coll, ns)));
		return type != null
				? list(SYM_CORE_APPLY, type, res)
				: res;
	}

	private static Object syntaxQuote(Object item, Namespace ns) {
		if (isUnquote(item)) {
			return unquote(item);
		}
		if (isUnquoteSplice(item)) {
			throw new LispException("'unquote-splicing' can only be used within a list");
		}
		if (isSeq(item)) {
			return syntaxQuoteColl(null, item, ns);
		}
		if (isVector(item)) {
			return syntaxQuoteColl(SYM_CORE_VECTOR, item, ns);
		}
		if (isSet(item)) {
			return syntaxQuoteColl(SYM_CORE_HASH_SET, item, ns);
		}
		if (isMap(item)) {
			Map<?, ?> m = (Map<?, ?>) item;
			List<Object> kvs = new ArrayList<>(m.size() * 2);
			for (Entry<?, ?> e : m.entrySet()) {
				kvs.add(e.getKey());
				kvs.add(e.getValue());
			}
			return syntaxQuoteColl(SYM_CORE_HASH_MAP, kvs, ns);
		}
		if (item instanceof Symbol) {
			Var var = ns.lookupVar((Symbol) item, false, true);
			if (var != null) {
				item = var.symbol;
			}
		}
		return list(SYM_CORE_QUOTE, item);
	}

	public static Object syntaxQuoteStar(Object item, Object ns) {
		return syntaxQuote(item, (Namespace) ns);
	}
}
