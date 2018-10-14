package de.npecomplete.mc.testproject.lisp.function;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;

public class ZeroArgFunction implements LispFunction {

	// The environment with which the function was created
	private final Environment env;

	private final Sequence body;

	public ZeroArgFunction(Environment env, Sequence body) {
		this.env = env;
		this.body = body;
	}

	@Override
	public Object apply() {
		return SpecialForm.DO.apply(body, env);
	}
}
