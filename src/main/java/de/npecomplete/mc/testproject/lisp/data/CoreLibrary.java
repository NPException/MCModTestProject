package de.npecomplete.mc.testproject.lisp.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import de.npecomplete.mc.testproject.lisp.LispException;
import de.npecomplete.mc.testproject.lisp.function.LispFunction;

public final class CoreLibrary {
	public static final LispFunction FN_LIST = (VarArgsFunction) ListSequence::new;

	public static final LispFunction FN_VECTOR =
			(VarArgsFunction) args -> Collections.unmodifiableList(Arrays.asList(args));

	public static final LispFunction FN_HASH_SET =
			(VarArgsFunction) args -> Collections.unmodifiableSet(new HashSet<>(Arrays.asList(args)));

	public static final LispFunction FN_HASH_MAP = (VarArgsFunction) args -> {
		if (args.length % 2 != 0) {
			throw new LispException("hash-map function only accepts even numbers of arguments");
		}
		Map<Object,Object> map = new HashMap<>();
		Iterator<Object> it = Arrays.asList(args).iterator();
		while (it.hasNext()) {
			map.put(it.next(), it.next());
		}
		return Collections.unmodifiableMap(map);
	};

	private CoreLibrary() {
		throw new IllegalStateException("No instance allowed.");
	}

	@FunctionalInterface
	private interface VarArgsFunction extends LispFunction {
		Object applyVarArgs(Object[] args);

		@Override
		default Object apply() {
			return applyVarArgs(new Object[0]);
		}

		@Override
		default Object apply(Object par1) {
			return applyVarArgs(new Object[] {par1});
		}

		@Override
		default Object apply(Object par1, Object par2) {
			return applyVarArgs(new Object[] {par1, par2});
		}

		@Override
		default Object apply(Object par1, Object par2, Object par3) {
			return applyVarArgs(new Object[] {par1, par2, par3});
		}

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
	}
}
