package de.npecomplete.mc.testproject.lisp.function;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;

public class FourPlusArgFunction implements LispFunction {

	// The environment with which the function was created
	private final Environment env;

	private final Sequence body;
	private final Symbol parSym1;
	private final Symbol parSym2;
	private final Symbol parSym3;
	private final Symbol parSym4;
	private final Symbol[] moreSym;

	public FourPlusArgFunction(Environment env, Sequence body,
			Symbol parSym1, Symbol parSym2, Symbol parSym3, Symbol parSym4,
			Symbol[] moreSym) {
		this.env = env;
		this.body = body;
		this.parSym1 = parSym1;
		this.parSym2 = parSym2;
		this.parSym3 = parSym3;
		this.parSym4 = parSym4;
		this.moreSym = moreSym;
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3, Object par4, Object... more) {
		if (more.length != moreSym.length) {
			throw new LispException("Wrong arity: " + (4 + more.length));
		}
		Environment localEnv = new Environment(env);
		localEnv.bind(parSym1, par1);
		localEnv.bind(parSym2, par2);
		localEnv.bind(parSym3, par3);
		localEnv.bind(parSym4, par4);

		int moreSize = more.length;
		if (moreSize > 0) {
			for (int i = 0; i < moreSize; i++) {
				localEnv.bind(moreSym[i], more[i]);
			}
		}
		return SpecialForm.DO.apply(body, localEnv);
	}
}
