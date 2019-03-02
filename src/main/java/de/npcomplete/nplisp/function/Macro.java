package de.npcomplete.nplisp.function;

import de.npcomplete.nplisp.data.Sequence;

public interface Macro {
	Object expand(Sequence args);
}
