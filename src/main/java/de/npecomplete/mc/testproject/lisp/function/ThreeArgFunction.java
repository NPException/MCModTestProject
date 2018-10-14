package de.npecomplete.mc.testproject.lisp.function;

import de.npecomplete.mc.testproject.lisp.Environment;
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

	public ThreeArgFunction(Environment env, Sequence body,
			Symbol parSym1, Symbol parSym2, Symbol parSym3) {
		this.env = env;
		this.body = body;
		this.parSym1 = parSym1;
		this.parSym2 = parSym2;
		this.parSym3 = parSym3;
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3) {
		Environment localEnv = new Environment(env);
		localEnv.bind(parSym1, par1);
		localEnv.bind(parSym2, par2);
		localEnv.bind(parSym3, par3);
		return SpecialForm.DO.apply(body, localEnv);
	}
}
