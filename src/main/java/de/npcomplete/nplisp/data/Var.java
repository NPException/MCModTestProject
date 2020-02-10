package de.npcomplete.nplisp.data;

import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.util.LispPrinter;

public final class Var implements LispFunction, Deref {
	private static final Object UNBOUND = new Object();

	public final Namespace ns;
	public final String name;
	private Object value = UNBOUND;

	public Var(Namespace ns, String name) {
		this.ns = ns;
		this.name = name;
	}

	public Var bindValue(Object value) {
		this.value = value;
		return this;
	}

	public Object deref() {
		if (value == UNBOUND) {
			throw new LispException("Var " + name + " is unbound.");
		}
		return value;
	}

	@Override
	public String toString() {
		return LispPrinter.printStr(this);
	}

	private LispFunction fn() {
		LispFunction f = LispFunction.from(deref());
		if (f == null) {
			throw new LispException("Can't create function from var " + name);
		}
		return f;
	}

	@Override
	public Object apply() {
		return fn().apply();
	}

	@Override
	public Object apply(Object par1) {
		return fn().apply(par1);
	}

	@Override
	public Object apply(Object par1, Object par2) {
		return fn().apply(par1, par2);
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3) {
		return fn().apply(par1, par2, par3);
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3, Object... more) {
		return fn().apply(par1, par2, par3, more);
	}

	@Override
	public Object applyTo(Sequence args) {
		return fn().applyTo(args);
	}
}
