package de.npecomplete.mc.testproject.lisp.function;

import java.util.Arrays;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.ListSequence;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;

public class ThreeArgFunction implements LispFunction {

	// The environment with which the function was created
	private final Environment env;

	private final Sequence body;
	private final Symbol parSym1;
	private final Symbol parSym2;
	private final Symbol parSym3;
	private final boolean varArgs;

	public ThreeArgFunction(Environment env, Sequence body,
			Symbol parSym1, Symbol parSym2, Symbol parSym3, boolean varArgs) {
		this.env = env;
		this.body = body;
		this.parSym1 = parSym1;
		this.parSym2 = parSym2;
		this.parSym3 = parSym3;
		this.varArgs = varArgs;
	}

	@Override
	public Object apply(Object par1, Object par2) {
		if (!varArgs) {
			throw new LispException("Wrong arity: 2");
		}
		Environment localEnv = new Environment(env);
		localEnv.bind(parSym1, par1);
		localEnv.bind(parSym2, par2);
		localEnv.bind(parSym3, ListSequence.EMPTY);
		return SpecialForm.DO.apply(body, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3) {
		Environment localEnv = new Environment(env);
		localEnv.bind(parSym1, par1);
		localEnv.bind(parSym2, par2);
		localEnv.bind(parSym3, varArgs ? new ListSequence(par3) : par3);
		return SpecialForm.DO.apply(body, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3, Object par4, Object... more) {
		int moreCount = more.length;
		if (!varArgs) {
			throw new LispException("Wrong arity: " + (4 + moreCount));
		}
		Object[] args = new Object[] {par3, par4};
		if (moreCount > 0) {
			args = Arrays.copyOf(args, 2 + moreCount);
			System.arraycopy(more, 0, args, 2, moreCount);
		}

		Environment localEnv = new Environment(env);
		localEnv.bind(parSym1, par1);
		localEnv.bind(parSym2, par2);
		localEnv.bind(parSym3, new ListSequence(args));
		return SpecialForm.DO.apply(body, localEnv);
	}
}
