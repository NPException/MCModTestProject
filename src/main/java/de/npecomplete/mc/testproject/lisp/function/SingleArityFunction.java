package de.npecomplete.mc.testproject.lisp.function;

import java.util.Arrays;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.ListSequence;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;
import de.npecomplete.mc.testproject.lisp.util.LispElf;
import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

public class SingleArityFunction implements LispFunction {

	private final Symbol name;
	private final boolean varArgs;

	// The environment with which the function was created
	private final Environment env;

	private final Sequence body;
	private final Symbol[] paramSymbols;

	public SingleArityFunction(Symbol name, Environment env, Sequence body, List<?> fnParams) {
		this.name = name;
		this.env = env;
		this.body = body;
		this.paramSymbols = validateFnParams(fnParams);
		this.varArgs = paramSymbols.length != fnParams.size();
	}

	private Environment initLocalEnv() {
		Environment localEnv = new Environment(env);
		if (name != null) {
			localEnv.bind(name, this);
		}
		return localEnv;
	}

	@Override
	public Object apply() {
		Environment localEnv = initLocalEnv();
		if (varArgs) {
			bindVarArgs(localEnv, LispElf.EMPTY_OBJECT_ARRAY);
		} else {
			assertArity(0);
		}
		return SpecialForm.DO.apply(body, localEnv);
	}

	@Override
	public Object apply(Object par1) {
		Environment localEnv = initLocalEnv();
		if (varArgs) {
			bindVarArgs(localEnv, par1);
		} else {
			assertArity(1);
			localEnv.bind(paramSymbols[0], par1);
		}
		return SpecialForm.DO.apply(body, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2) {
		Environment localEnv = initLocalEnv();
		if (varArgs) {
			bindVarArgs(localEnv, par1, par2);
		} else {
			assertArity(2);
			localEnv.bind(paramSymbols[0], par1);
			localEnv.bind(paramSymbols[1], par2);
		}
		return SpecialForm.DO.apply(body, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3) {
		Environment localEnv = initLocalEnv();
		if (varArgs) {
			bindVarArgs(localEnv, par1, par2, par3);
		} else {
			assertArity(3);
			localEnv.bind(paramSymbols[0], par1);
			localEnv.bind(paramSymbols[1], par2);
			localEnv.bind(paramSymbols[2], par3);
		}
		return SpecialForm.DO.apply(body, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3, Object par4, Object... more) {
		Environment localEnv = initLocalEnv();

		if (varArgs) {
			Object[] args = new Object[] {par1, par2, par3, par4};
			int moreCount = more.length;
			if (moreCount > 0) {
				args = Arrays.copyOf(args, 4 + moreCount);
				System.arraycopy(more, 0, args, 4, moreCount);
			}
			bindVarArgs(localEnv, args);

		} else {
			int moreCount = more.length;
			assertArity(4 + moreCount);
			localEnv.bind(paramSymbols[0], par1);
			localEnv.bind(paramSymbols[1], par2);
			localEnv.bind(paramSymbols[2], par3);
			localEnv.bind(paramSymbols[3], par4);
			for (int i = 0; i < moreCount; i++) {
				localEnv.bind(paramSymbols[i + 4], more[i]);
			}
		}

		return SpecialForm.DO.apply(body, localEnv);
	}

	private void assertArity(int arity) {
		if (paramSymbols.length != arity) {
			throw new LispException("Wrong arity: " + arity + ". Expected: =" + paramSymbols.length);
		}
	}

	private void bindVarArgs(Environment localEnv, Object... args) {
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
				? new ListSequence(Arrays.copyOfRange(args, lastParamIndex, argsCount))
				: Sequence.EMPTY_SEQUENCE;
		localEnv.bind(paramSymbols[lastParamIndex], varArgs);
	}

	private static boolean isVarArgsIndicator(Symbol sym) {
		return sym.name.equals("&");
	}

	/**
	 * Validates the function arguments and returns a Symbol array to be used
	 * by the function. If the array has a different size than the input list,
	 * the function is a varargs function.
	 */
	public static Symbol[] validateFnParams(List<?> fnArgs) {
		int length = fnArgs.size();
		if (length == 0) {
			return LispElf.EMPTY_SYMBOL_ARRAY;
		}

		// verify function argument list
		for (Object sym : fnArgs) {
			if (!(sym instanceof Symbol)) {
				String s = LispPrinter.printStr(sym);
				throw new LispException("'fn' argument is not a symbol: " + s);
			}
		}

		@SuppressWarnings("SuspiciousToArrayCall")
		Symbol[] symbols = fnArgs.toArray(new Symbol[0]);

		int varArgsIndex = length - 2; // indicator expected as second to last symbol
		boolean varArgs = false;

		for (int i = 0; i < length; i++) {
			if (isVarArgsIndicator(symbols[i])) {
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
