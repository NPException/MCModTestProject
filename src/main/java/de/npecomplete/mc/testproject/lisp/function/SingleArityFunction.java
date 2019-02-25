package de.npecomplete.mc.testproject.lisp.function;

import java.util.Arrays;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;
import de.npecomplete.mc.testproject.lisp.util.LispElf;

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
		return SpecialForm.DO.apply(body, localEnv);
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
		return SpecialForm.DO.apply(body, localEnv);
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
		return SpecialForm.DO.apply(body, localEnv);
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
		return SpecialForm.DO.apply(body, localEnv);
	}

	@SuppressWarnings("Duplicates")
	@Override
	public Object apply(Object par1, Object par2, Object par3, Object par4, Object... more) {
		Environment localEnv = initLocalEnv();
		int moreCount = more.length;

		if (variadic) {
			Object[] args = new Object[] {par1, par2, par3, par4};
			if (moreCount > 0) {
				args = Arrays.copyOf(args, 4 + moreCount);
				System.arraycopy(more, 0, args, 4, moreCount);
			}
			LispElf.bindVarArgs(localEnv, paramSymbols, args);

		} else {
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
}
