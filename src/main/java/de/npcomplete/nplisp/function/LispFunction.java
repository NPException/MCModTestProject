package de.npcomplete.nplisp.function;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.data.Sequence;

@SuppressWarnings("rawtypes")
public interface LispFunction extends Runnable, Callable, Supplier, Consumer, Predicate, Function {
	default Object apply() {
		throw new LispException("Wrong arity: 0");
	}

	default Object apply(Object par1) {
		throw new LispException("Wrong arity: 1");
	}

	default Object apply(Object par1, Object par2) {
		throw new LispException("Wrong arity: 2");
	}

	default Object apply(Object par1, Object par2, Object par3) {
		throw new LispException("Wrong arity: 3");
	}

	default Object apply(Object par1, Object par2, Object par3, Object... more) {
		throw new LispException("Wrong arity: " + (3 + more.length));
	}

	default Object applyTo(Sequence args) {
		if (args == null || args.empty()) {
			return apply();
		}
		Object par1 = args.first();
		args = args.next();
		if (args == null) {
			return apply(par1);
		}
		Object par2 = args.first();
		args = args.next();
		if (args == null) {
			return apply(par1, par2);
		}
		Object par3 = args.first();
		args = args.next();
		if (args == null) {
			return apply(par1, par2, par3);
		}

		ArrayList<Object> rest = new ArrayList<>();
		do {
			rest.add(args.first());
		} while ((args = args.next()) != null);

		return apply(par1, par2, par3, rest.toArray());
	}

	// Runnable
	@Override
	default void run() {
		apply();
	}

	// Callable
	@Override
	default Object call() {
		return apply();
	}

	// Supplier
	@Override
	default Object get() {
		return apply();
	}

	// Consumer
	@Override
	default void accept(Object o) {
		apply(o);
	}

	// Predicate
	@Override
	default boolean test(Object o) {
		return (Boolean) apply(o);
	}
}
