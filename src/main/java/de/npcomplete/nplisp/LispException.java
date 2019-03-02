package de.npcomplete.nplisp;

public class LispException extends RuntimeException {

	public LispException(String msg) {
		super(msg);
	}

	public LispException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
