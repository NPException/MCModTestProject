package de.npcomplete.nplisp.function;

import java.util.ArrayList;

import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.data.Sequence;

@SuppressWarnings("unchecked")
public interface LispFunction {
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
}
