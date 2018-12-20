package util;

public class AssistantBotException extends Exception {
	private static final long serialVersionUID = 1L;
	public static enum Type {
		ARITHMETICPARSE, COMMENTPARSE,SCRIPTHELPERARRAY,NOTONEMAINLABEL
	};
	private Type t_;
	public AssistantBotException(Type t,String msg) {
		super(msg);
		t_ = t;
	}
	public Type getType() {
		return t_;
	}
}
