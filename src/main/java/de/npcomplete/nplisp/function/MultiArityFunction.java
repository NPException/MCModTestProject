package de.npcomplete.nplisp.function;

import java.util.Arrays;
import java.util.List;

import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.core.Environment;
import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.data.Symbol;
import de.npcomplete.nplisp.util.LispElf;

public final class MultiArityFunction implements LispFunction {

	private final Symbol name;
	// First array index is function arity. The resulting Object array
	// has the function body at index 0 and the argument symbols at index 1.
	private final Object[][] functions;
	private final Object[] variadicFnData;

	// The environment with which the function was created
	private final Environment env;

	private MultiArityFunction(Symbol name, boolean variadic, Object[][] functions, Environment env) {
		this.name = name;
		this.functions = functions;
		variadicFnData = variadic ? functions[functions.length - 1] : null;
		this.env = env;
	}

	private Environment initLocalEnv() {
		Environment localEnv = new Environment(env);
		if (name != null) {
			localEnv.bind(name, this);
		}
		return localEnv;
	}

	private Object[] fnData(int arity) {
		Object[] fnData = functions.length > arity
				? functions[arity]
				: variadicFnData;
		if (fnData == null) {
			if (variadicFnData != null && arity == functions.length - 2) {
				return variadicFnData;
			}
			throw new LispException("Wrong arity: " + arity);
		}
		return fnData;
	}

	@Override
	public Object apply() {
		Environment localEnv = initLocalEnv();
		Object[] fnData = fnData(0);
		Sequence body = (Sequence) fnData[0];
		Symbol[] paramSymbols = (Symbol[]) fnData[1];

		if (fnData == variadicFnData) {
			LispElf.bindVarArgs(localEnv, paramSymbols, LispElf.EMPTY_OBJECT_ARRAY);
		}
		return SingleArityFunction.call(body, paramSymbols, localEnv);
	}

	@Override
	public Object apply(Object par1) {
		Environment localEnv = initLocalEnv();
		Object[] fnData = fnData(1);
		Sequence body = (Sequence) fnData[0];
		Symbol[] paramSymbols = (Symbol[]) fnData[1];

		if (fnData == variadicFnData) {
			LispElf.bindVarArgs(localEnv, paramSymbols, par1);
		} else {
			localEnv.bind(paramSymbols[0], par1);
		}
		return SingleArityFunction.call(body, paramSymbols, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2) {
		Environment localEnv = initLocalEnv();
		Object[] fnData = fnData(2);
		Sequence body = (Sequence) fnData[0];
		Symbol[] paramSymbols = (Symbol[]) fnData[1];

		if (fnData == variadicFnData) {
			LispElf.bindVarArgs(localEnv, paramSymbols, par1, par2);
		} else {
			localEnv.bind(paramSymbols[0], par1);
			localEnv.bind(paramSymbols[1], par2);
		}
		return SingleArityFunction.call(body, paramSymbols, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3) {
		Environment localEnv = initLocalEnv();
		Object[] fnData = fnData(3);
		Sequence body = (Sequence) fnData[0];
		Symbol[] paramSymbols = (Symbol[]) fnData[1];

		if (fnData == variadicFnData) {
			LispElf.bindVarArgs(localEnv, paramSymbols, par1, par2, par3);
		} else {
			localEnv.bind(paramSymbols[0], par1);
			localEnv.bind(paramSymbols[1], par2);
			localEnv.bind(paramSymbols[2], par3);
		}
		return SingleArityFunction.call(body, paramSymbols, localEnv);
	}

	@SuppressWarnings("Duplicates")
	@Override
	public Object apply(Object par1, Object par2, Object par3, Object... more) {
		Environment localEnv = initLocalEnv();
		int moreCount = more.length;
		Object[] fnData = fnData(3 + moreCount);
		Sequence body = (Sequence) fnData[0];
		Symbol[] paramSymbols = (Symbol[]) fnData[1];

		if (fnData == variadicFnData) {
			Object[] args = new Object[3 + moreCount];
			args[0] = par1;
			args[1] = par2;
			args[2] = par3;
			System.arraycopy(more, 0, args, 3, moreCount);
			LispElf.bindVarArgs(localEnv, paramSymbols, args);

		} else {
			localEnv.bind(paramSymbols[0], par1);
			localEnv.bind(paramSymbols[1], par2);
			localEnv.bind(paramSymbols[2], par3);
			for (int i = 0; i < moreCount; i++) {
				localEnv.bind(paramSymbols[i + 3], more[i]);
			}
		}

		return SingleArityFunction.call(body, paramSymbols, localEnv);
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
			Symbol[] paramSymbols = LispElf.validateFnParams(fnArgs);
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
			Object[] functionData = functions[arity] = new Object[2];
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
