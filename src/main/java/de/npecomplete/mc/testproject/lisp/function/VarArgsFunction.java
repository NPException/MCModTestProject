package de.npecomplete.mc.testproject.lisp.function;

import java.util.ArrayList;
import java.util.Arrays;

import de.npecomplete.mc.testproject.lisp.data.Sequence;
import de.npecomplete.mc.testproject.lisp.util.LispElf;

@FunctionalInterface
public interface VarArgsFunction extends LispFunction {
	Object applyVarArgs(Object... args);

	@Override
	default Object apply() {
		return applyVarArgs(LispElf.EMPTY_OBJECT_ARRAY);
	}

	@Override
	default Object apply(Object par1) {
		return applyVarArgs(par1);
	}

	@Override
	default Object apply(Object par1, Object par2) {
		return applyVarArgs(par1, par2);
	}

	@Override
	default Object apply(Object par1, Object par2, Object par3) {
		return applyVarArgs(par1, par2, par3);
	}

	@SuppressWarnings("Duplicates")
	@Override
	default Object apply(Object par1, Object par2, Object par3, Object par4, Object... more) {
		int moreCount = more.length;
		Object[] args = new Object[] {par1, par2, par3, par4};
		if (moreCount > 0) {
			args = Arrays.copyOf(args, 4 + moreCount);
			System.arraycopy(more, 0, args, 4, moreCount);
		}
		return applyVarArgs(args);
	}

	@Override
	default Object applyTo(Sequence args) {
		ArrayList<Object> l = new ArrayList<>();
		while (args != null && !args.empty()) {
			l.add(args.first());
			args = args.next();
		}
		return applyVarArgs(l.toArray());
	}
}
