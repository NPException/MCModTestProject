package de.npecomplete.mc.testproject.lisp.function;

import java.util.Arrays;
import java.util.List;

import de.npecomplete.mc.testproject.lisp.Environment;
import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.data.Symbol;
import de.npecomplete.mc.testproject.lisp.special.SpecialForm;
import de.npecomplete.mc.testproject.lisp.util.LispPrinter;

public class MultiArityFunction implements LispFunction {

	// The environment with which the function was created
	private final Environment env;

	// Function body for calls without arguments
	private Sequence noArgsBody;

	// Function body and argument symbol for calls with one argument
	private Sequence oneArgBody;
	private Symbol oneArgSymbol;

	// Function body and argument symbols for calls with two arguments
	private Sequence twoArgBody;
	private Symbol[] twoArgSymbols;

	// Function body and argument symbols for calls with three arguments
	private Sequence threeArgBody;
	private Symbol[] threeArgSymbols;

	// Function body and argument symbols for calls with 4 or more arguments.
	// First array index is function arity. The resulting Object array
	// has the function body as it's first argument, and the argument
	// symbols after it.
	private Object[][] multiArgFunctions;

	public MultiArityFunction(Environment env) {
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
		List<?> fnArgs = (List) arg1;
		// verify function argument list
		for (Object sym : fnArgs) {
			if (!(sym instanceof Symbol)) {
				String s = LispPrinter.printStr(sym);
				throw new LispException("'fn' arity definition argument is not a symbol: " + s);
			}
		}

		Sequence body = args.next();
		Symbol[] symbols = fnArgs.toArray(new Symbol[0]);
		switch (symbols.length) {
			case 0:
				if (noArgsBody == null) {
					noArgsBody = body;
					return;
				}
				break;
			case 1:
				if (oneArgBody == null) {
					oneArgBody = body;
					oneArgSymbol = symbols[0];
					return;
				}
				break;
			case 2:
				if (twoArgBody == null) {
					twoArgBody = body;
					twoArgSymbols = symbols;
					return;
				}
				break;
			case 3:
				if (threeArgBody == null) {
					threeArgBody = body;
					threeArgSymbols = symbols;
					return;
				}
				break;
			default:
				int multiArgIndex = symbols.length - 4;
				if (multiArgFunctions == null) {
					multiArgFunctions = new Object[multiArgIndex + 1][];
				}
				if (multiArgIndex >= multiArgFunctions.length) {
					multiArgFunctions = Arrays.copyOf(multiArgFunctions, multiArgIndex + 1);
				}
				if (multiArgFunctions[multiArgIndex] == null) {
					Object[] functionData = new Object[symbols.length + 1];
					functionData[0] = body;
					System.arraycopy(symbols, 0, functionData, 1, symbols.length);
					multiArgFunctions[multiArgIndex] = functionData;
					return;
				}
		}
		throw new LispException("Function body with arity " + symbols.length + " already exists");
	}

	@Override
	public Object apply() {
		if (noArgsBody == null) {
			throw new LispException("Wrong arity: 0");
		}
		return SpecialForm.DO.apply(noArgsBody, env);
	}

	@Override
	public Object apply(Object par) {
		if (oneArgBody == null) {
			throw new LispException("Wrong arity: 1");
		}
		Environment localEnv = new Environment(env);
		localEnv.bind(oneArgSymbol, par);
		return SpecialForm.DO.apply(oneArgBody, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2) {
		if (twoArgBody == null) {
			throw new LispException("Wrong arity: 2");
		}
		Environment localEnv = new Environment(env);
		localEnv.bind(twoArgSymbols[0], par1);
		localEnv.bind(twoArgSymbols[1], par2);
		return SpecialForm.DO.apply(twoArgBody, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3) {
		if (threeArgBody == null) {
			throw new LispException("Wrong arity: 3");
		}
		Environment localEnv = new Environment(env);
		localEnv.bind(threeArgSymbols[0], par1);
		localEnv.bind(threeArgSymbols[1], par2);
		localEnv.bind(threeArgSymbols[2], par3);
		return SpecialForm.DO.apply(threeArgBody, localEnv);
	}

	@Override
	public Object apply(Object par1, Object par2, Object par3, Object par4, Object... more) {
		Object[] functionData = multiArgFunctions != null
				? multiArgFunctions[more.length]
				: null;
		if (functionData == null) {
			throw new LispException("Wrong arity: " + (4 + more.length));
		}
		Sequence multiArgBody = (Sequence) functionData[0];
		Environment localEnv = new Environment(env);
		localEnv.bind((Symbol) functionData[1], par1);
		localEnv.bind((Symbol) functionData[2], par2);
		localEnv.bind((Symbol) functionData[3], par3);
		localEnv.bind((Symbol) functionData[4], par4);
		for (int i = 0, size = more.length; i < size; i++) {
			localEnv.bind((Symbol) functionData[i + 5], more[i]);
		}
		return SpecialForm.DO.apply(multiArgBody, localEnv);
	}
}
