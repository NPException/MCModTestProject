package de.npecomplete.mc.testproject.lisp.function;

import java.util.Arrays;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;

public final class MultiArityFunction implements LispFunction {

	private final Symbol name;
	private final boolean variadic;
	// First array index is function arity. The resulting Object array
	// has the function body at index 0 and the argument symbols at index 1.
	private final Object[][] functions;

	// The environment with which the function was created
	private final Environment env;

	private MultiArityFunction(Symbol name, boolean variadic, Object[][] functions, Environment env) {
		this.name = name;
		this.variadic = variadic;
		this.functions = functions;
		this.env = env;
	}

	@Override
	public Object apply() {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public Object apply(Object par) {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public Object apply(Object par1, Object par2) {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3) {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3, Object par4, Object... more) {
		throw new UnsupportedOperationException(); // TODO
	}

	public static class Builder {
		private static final Object[][] NO_FUNCTIONS = new Object[0][];

		private final Symbol name;
		private boolean variadic;

		// The environment with which the function was created
		private final Environment env;

		// First array index is function arity. The resulting Object array
		// has the function body at index 0 and the argument symbols at index 1.
		private Object[][] functions = NO_FUNCTIONS;

		public Builder(Symbol name, Environment env) {
			this.name = name;
			this.env = env;
		}

		public void addArity(Sequence args) {
			if (args.empty()) {
				throw new LispException("'fn' arity definition requires at least one element: ([ARGS] *&BODY*)");
			}
			Object arg1 = args.first();
			if (!(arg1 instanceof List)) {
				throw new LispException("'fn' arity definition first element is not a List");
			}
			List fnArgs = (List) arg1;

			Sequence body = args.next();
			Symbol[] paramSymbols = SingleArityFunction.validateFnParams(fnArgs);
			int arity = paramSymbols.length;
			boolean newVariadic = arity != fnArgs.size();

			if (variadic && newVariadic) {
				throw new LispException("Can only have one variadic arity definition");
			}

			if (arity >= functions.length) {
				if (variadic) {
					throw new LispException("Can't have fixed arity function with more params than variadic function");
				}
				functions = Arrays.copyOf(functions, arity + 1);
			} else if (newVariadic) {
				throw new LispException("Can't have fixed arity function with more params than variadic function");
			}
			if (functions[arity] != null) {
				throw new LispException("Function body definition with arity " + arity + " already exists");
			}
			Object[] functionData = functions[arity] = new Object[3];
			functionData[0] = body;
			functionData[1] = paramSymbols;
			if (newVariadic) {
				variadic = true;
			}
		}

		public MultiArityFunction build() {
			return new MultiArityFunction(name, variadic, functions, env);
		}
	}
}
