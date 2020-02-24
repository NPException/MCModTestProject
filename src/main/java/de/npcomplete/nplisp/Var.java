package de.npcomplete.nplisp;

import de.npcomplete.nplisp.data.Deref;
import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.data.Symbol;
import de.npcomplete.nplisp.function.LispFunction;
import de.npcomplete.nplisp.function.LispFunctionFactory;
import de.npcomplete.nplisp.util.LispPrinter;

public class Var implements LispFunction, Deref {
	private static final Object UNBOUND = new Object();

	public final Symbol symbol;
	private Object value = UNBOUND;
	private boolean isMacro;
	private boolean isPrivate;

	public Var(Symbol symbol) {
		if (symbol.nsName == null) {
			throw new LispException("Can't create var without fully qualified symbol");
		}
		this.symbol = symbol;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public Var setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
		return this;
	}

	public boolean isMacro() {
		return isMacro;
	}

	public Var macro(boolean isMacro) {
		this.isMacro = isMacro;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Var)) {
			return false;
		}
		Var other = (Var) o;
		return symbol.equals(other.symbol);
	}

	@Override
	public int hashCode() {
		return symbol.hashCode();
	}

	public Var bind(Object value) {
		this.value = value;
		return this;
	}

	public Object deref() {
		if (value == UNBOUND) {
			throw new LispException("Var " + this + " is unbound.");
		}
		return value;
	}

	@Override
	public String toString() {
		return LispPrinter.prStr(this);
	}

	private LispFunction fn() {
		LispFunction f = LispFunctionFactory.from(deref());
		if (f == null) {
			throw new LispException("Can't create function from var " + this);
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


	public static final class MarkerVar extends Var {
		public MarkerVar(Symbol symbol) {
			super(symbol);
		}

		private LispException forbiddenModification() {
			return new LispException("Var must not be modified: " + this);
		}

		@Override
		public Var bind(Object value) {
			throw forbiddenModification();
		}

		@Override
		public Var macro(boolean isMacro) {
			throw forbiddenModification();
		}
	}
}
