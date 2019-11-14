package de.npcomplete.nplisp.function;

import java.util.ArrayList;

import de.npcomplete.nplisp.data.Sequence;
import de.npcomplete.nplisp.util.LispElf;

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
	default Object apply(Object par1, Object par2, Object par3, Object... more) {
		int moreCount = more.length;
		Object[] args = new Object[3 + moreCount];
		args[0] = par1;
		args[1] = par2;
		args[2] = par3;
		System.arraycopy(more, 0, args, 3, moreCount);
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
