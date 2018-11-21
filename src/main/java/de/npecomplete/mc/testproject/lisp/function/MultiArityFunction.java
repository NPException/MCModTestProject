package de.npecomplete.mc.testproject.lisp.function;

import java.util.Arrays;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;

public class MultiArityFunction implements LispFunction {

	private final Symbol name;

	// The environment with which the function was created
	private final Environment env;

	// First array index is function arity. The resulting Object array
	// has the function body at index 0, the argument symbols at index 1,
	// and a Boolean indicating a varargs at index 2
	private Object[][] functions;

	public MultiArityFunction(Symbol name, Environment env) {
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
		boolean varArgs = arity != fnArgs.size();

		// TODO, verify that no fixed arity function has more fixed arguments than the variadic function

		if (functions == null) {
			functions = new Object[arity + 1][];
		}
		if (arity >= functions.length) {
			functions = Arrays.copyOf(functions, arity + 1);
		}
		if (functions[arity] == null) {
			Object[] functionData = functions[arity] = new Object[3];
			functionData[0] = body;
			functionData[1] = paramSymbols;
			functionData[2] = varArgs;
			return;
		}
		throw new LispException("Function body with arity " + arity + " already exists");
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
}
