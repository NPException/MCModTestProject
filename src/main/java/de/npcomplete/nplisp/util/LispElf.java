package de.npcomplete.nplisp.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import de.npcomplete.nplisp.Environment;
import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.data.ListSequence;
import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.data.Symbol;

public final class LispElf {
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	public static final Symbol[] EMPTY_SYMBOL_ARRAY = new Symbol[0];

	/**
	 * @return true if the given object
	 * is not null and not false.
	 */
	public static boolean truthy(Object o) {
		return !falsy(o);
	}

	/**
	 * @return true if the given object
	 * is null or false.
	 */
	public static boolean falsy(Object o) {
		return o == null ||
				o instanceof Boolean && !(Boolean) o;
	}

	/**
	 * Checks if the given sequence has the specified
	 * number of elements.
	 */
	public static boolean matchSize(Sequence seq, int min, int max) {
		return minSize(seq, min) && maxSize(seq, max);
	}

	/**
	 * @return true if the given {@link Sequence} is not null
	 * and contains at least 'min' elements.
	 */
	public static boolean minSize(Sequence seq, int min) {
		if (seq == null || min > 0 && seq.empty()) {
			return false;
		}
		while (--min > 0) {
			seq = seq.next();
			if (seq == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true if the given {@link Sequence} is not null
	 * and contains at most 'max' elements.
	 */
	public static boolean maxSize(Sequence seq, int max) {
		if (seq == null || max <= 0 && !seq.empty()) {
			return false;
		}
		while ((seq = seq.next()) != null) {
			if (--max == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns an iterator that maps the contents of the given iterator using the mapping function
	 */
	public static <T, R> Iterator<R> mapIterator(Iterator<T> it, Function<T, R> mapping) {
		Objects.requireNonNull(it);
		Objects.requireNonNull(mapping);
		return new Iterator<R>() {
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public R next() {
				return mapping.apply(it.next());
			}
		};
	}

	public static void bindVarArgs(Environment localEnv, Symbol[] paramSymbols, Object... args) {
		int argsCount = args.length;
		int paramSymCount = paramSymbols.length;
		int lastParamIndex = paramSymCount - 1;
		if (paramSymCount > argsCount + 1) {
			throw new LispException("Wrong arity: " + argsCount + ". Expected: >=" + lastParamIndex);
		}

		for (int i = 0; i < lastParamIndex; i++) {
			localEnv.bind(paramSymbols[i], args[i]);
		}
		Sequence varArgs = args.length > lastParamIndex
				? new ListSequence(lastParamIndex == 0 ? args : Arrays.copyOfRange(args, lastParamIndex, argsCount))
				: null;
		localEnv.bind(paramSymbols[lastParamIndex], varArgs);
	}

	/**
	 * Validates the function arguments and returns a Symbol array to be used
	 * by the function. If the array has a different size than the input list,
	 * the function is a varargs function.
	 */
	public static Symbol[] validateFnParams(List<?> fnArgs) {
		int length = fnArgs.size();
		if (length == 0) {
			return EMPTY_SYMBOL_ARRAY;
		}

		// verify function argument list
		for (Object sym : fnArgs) {
			if (!(sym instanceof Symbol)) {
				String s = LispPrinter.prStr(sym);
				throw new LispException("'fn' argument is not a symbol: " + s);
			}
		}

		@SuppressWarnings("SuspiciousToArrayCall")
		Symbol[] symbols = fnArgs.toArray(new Symbol[0]);

		int varArgsIndex = length - 2; // indicator expected as second to last symbol
		boolean varArgs = false;

		for (int i = 0; i < length; i++) {
			if (symbols[i].name.equals("&")) { // indicator check
				varArgs = true;
				if (i != varArgsIndex) {
					throw new LispException("Varargs indicator '&' must be in second to last position.");
				}
			}
		}

		if (varArgs) {
			// cut out indicator
			Symbol last = symbols[length - 1];
			symbols[length - 2] = last;
			symbols = Arrays.copyOf(symbols, length - 1);
		}

		return symbols;
	}
}
