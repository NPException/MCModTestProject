package de.npcomplete.nplisp.function;

import java.util.List;

import de.npcomplete.nplisp.LispException;
import de.npcomplete.nplisp.core.Environment;
import de.npcomplete.nplisp.corelibrary.CoreLibrary.TailCall;
import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.data.Symbol;
import de.npcomplete.nplisp.util.LispElf;

public final class SingleArityFunction implements LispFunction {

	private final Symbol name;
	private final boolean variadic;

	// The environment with which the function was created
	private final Environment env;

	private final Sequence body;
	private final Symbol[] paramSymbols;

	public SingleArityFunction(Symbol name, Environment env, Sequence body, List<?> fnParams) {
		this.name = name;
		this.env = env;
		this.body = body;
		this.paramSymbols = LispElf.validateFnParams(fnParams);
		this.variadic = paramSymbols.length != fnParams.size();
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
		if (variadic) {
			LispElf.bindVarArgs(localEnv, paramSymbols, LispElf.EMPTY_OBJECT_ARRAY);
		} else {
			assertArity(0);
		}
		return call(body, paramSymbols, localEnv);
	}

	@Override
	public Object apply(Object par1) {
		Environment localEnv = initLocalEnv();
		if (variadic) {
			LispElf.bindVarArgs(localEnv, paramSymbols, par1);
		} else {
			assertArity(1);
			localEnv.bind(paramSymbols[0], par1);
		}
		return call(body, paramSymbols, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2) {
		Environment localEnv = initLocalEnv();
		if (variadic) {
			LispElf.bindVarArgs(localEnv, paramSymbols, par1, par2);
		} else {
			assertArity(2);
			localEnv.bind(paramSymbols[0], par1);
			localEnv.bind(paramSymbols[1], par2);
		}
		return call(body, paramSymbols, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3) {
		Environment localEnv = initLocalEnv();
		if (variadic) {
			LispElf.bindVarArgs(localEnv, paramSymbols, par1, par2, par3);
		} else {
			assertArity(3);
			localEnv.bind(paramSymbols[0], par1);
			localEnv.bind(paramSymbols[1], par2);
			localEnv.bind(paramSymbols[2], par3);
		}
		return call(body, paramSymbols, localEnv);
	}

	@SuppressWarnings("Duplicates")
	@Override
	public Object apply(Object par1, Object par2, Object par3, Object... more) {
		Environment localEnv = initLocalEnv();
		int moreCount = more.length;

		if (variadic) {
			Object[] args = new Object[3 + moreCount];
			args[0] = par1;
			args[1] = par2;
			args[2] = par3;
			System.arraycopy(more, 0, args, 3, moreCount);
			LispElf.bindVarArgs(localEnv, paramSymbols, args);

		} else {
			assertArity(3 + moreCount);
			localEnv.bind(paramSymbols[0], par1);
			localEnv.bind(paramSymbols[1], par2);
			localEnv.bind(paramSymbols[2], par3);
			for (int i = 0; i < moreCount; i++) {
				localEnv.bind(paramSymbols[i + 3], more[i]);
			}
		}

		return call(body, paramSymbols, localEnv);
	}

	/**
	 * Runs the function body, with the prepared local environment
	 * and handles explicit recursive tail calls.
	 */
	static Object call(Sequence body, Symbol[] paramSymbols, Environment preparedLocalEnv) {
		Object val;
		while ((val = SpecialForm.DO(body, preparedLocalEnv, true)) instanceof TailCall) {
			TailCall tailCall = (TailCall) val;
			Object[] tcArgs = tailCall.args;
			int length = tcArgs.length;
			if (length != paramSymbols.length) {
				throw new LispException("'recur' did not match required arity." +
						" Expected: " + paramSymbols.length + ". Actual: " + length);
			}
			for (int i = 0; i < length; i++) {
				preparedLocalEnv.bind(paramSymbols[i], tcArgs[i]);
			}
		}
		return val;
	}

	private void assertArity(int arity) {
		if (paramSymbols.length != arity) {
			throw new LispException("Wrong arity: " + arity + ". Expected: " + paramSymbols.length);
		}
	}
}
