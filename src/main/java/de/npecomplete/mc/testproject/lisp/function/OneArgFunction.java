package de.npecomplete.mc.testproject.lisp.function;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;

public class OneArgFunction implements LispFunction {

	// The environment with which the function was created
	private final Environment env;

	private final Sequence body;
	private final Symbol parSym1;

	public OneArgFunction(Environment env, Sequence body,
			Symbol parSym1) {
		this.env = env;
		this.body = body;
		this.parSym1 = parSym1;
	}

	@Override
	public Object apply(Object par1) {
		Environment localEnv = new Environment(env);
		localEnv.bind(parSym1, par1);
		return SpecialForm.DO.apply(body, localEnv);
	}
}
