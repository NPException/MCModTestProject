package de.npcomplete.nplisp.data;

import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.function.VarArgsFunction;

public final class Var implements LispFunction, Deref {
	private final Symbol name; // fully qualified symbol
	private Object value;

	Var(Symbol name) {
		this.name = name;
		value = new Unbound(this);
	}

	public void bind(Object value) {
		this.value = value;
	}

	public Object deref() {
		return value;
	}

	@Override
	public String toString() {
		return "#'" + name.toString();
	}

	private LispFunction fn() {
		LispFunction f = LispFunction.from(value);
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
	public Object apply(Object par1, Object par2, Object par3, Object par4, Object... more) {
		return fn().apply(par1, par2, par3, par4, more);
	}

	@Override
	public Object applyTo(Sequence args) {
		return fn().applyTo(args);
	}

	private static final class Unbound implements VarArgsFunction {
		private final Var v;

		Unbound(Var v) {
			this.v = v;
		}

		@Override
		public Object applyVarArgs(Object... args) {
			throw new LispException("Attempting to call unbound fn: " + v.toString());
		}

		@Override
		public String toString() {
			return "Unbound " + v.toString();
		}
	}
}
