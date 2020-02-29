package de.npcomplete.nplisp.core;

import de.npcomplete.nplisp.LispException;
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

	private String doc;

	private boolean isFixed;

	public Var(Symbol symbol) {
		if (symbol.nsName == null) {
			throw new LispException("Can't create var without fully qualified symbol");
		}
		this.symbol = symbol;
	}

	public Var markFixed() {
		isFixed = true;
		return this;
	}

	public boolean isFixed() {
		return isFixed;
	}

	private void prepareModification() {
		if (isFixed) {
			throw new LispException("Var must not be modified: " + this);
		}
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public Var setPrivate(boolean isPrivate) {
		prepareModification();
		this.isPrivate = isPrivate;
		return this;
	}

	public boolean isMacro() {
		return isMacro;
	}

	public Var macro(boolean isMacro) {
		prepareModification();
		this.isMacro = isMacro;
		return this;
	}

	public String doc() {
		return doc;
	}

	public Var doc(String doc) {
		this.doc = doc;
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
		if (this.value != value) {
			prepareModification();
			this.value = value;
		}
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
}
