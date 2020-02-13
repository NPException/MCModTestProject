package de.npcomplete.nplisp.function;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.npcomplete.nplisp.LispException;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class LispFunctionFactory {
	private LispFunctionFactory() {}

	/**
	 * Attempts to create an {@link LispFunction} from the given object.
	 */
	public static LispFunction from(Object o) {
		if (o instanceof LispFunction) {
			return (LispFunction) o;
		}

		if (o instanceof Set) {
			return (Fn1)
					par -> ((Set) o).contains(par) ? par : null;
		}

		if (o instanceof Map) {
			return (Fn1) ((Map) o)::get;
		}

		if (o instanceof Runnable) {
			return (Fn0) () -> {
				((Runnable) o).run();
				return null;
			};
		}

		if (o instanceof Function) {
			return (Fn1) ((Function) o)::apply;
		}

		if (o instanceof Supplier) {
			return (Fn0) ((Supplier) o)::get;
		}

		if (o instanceof Consumer) {
			return (Fn1) par -> {
				((Consumer) o).accept(par);
				return null;
			};
		}

		// TODO: maybe wrap most of Java's functional interfaces here

		if (o instanceof Callable) {
			return (Fn0) () -> {
				try {
					return ((Callable) o).call();
				} catch (Exception e) {
					throw new LispException("Failed to call Callable", e);
				}
			};
		}

		return null;
	}

	@FunctionalInterface
	public interface Fn0 extends LispFunction {
		@Override
		Object apply();
	}

	@FunctionalInterface
	public interface Fn1 extends LispFunction {
		@Override
		Object apply(Object par1);
	}

	@FunctionalInterface
	public interface Fn2 extends LispFunction {
		@Override
		Object apply(Object par1, Object par2);
	}

	@FunctionalInterface
	public interface Fn3 extends LispFunction {
		@Override
		Object apply(Object par1, Object par2, Object par3);
	}
}
