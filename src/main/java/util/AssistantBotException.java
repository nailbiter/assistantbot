package util;

public class AssistantBotException extends Exception {
	private static final long serialVersionUID = 1L;
	public static enum Type {
		ARITHMETICPARSE, COMMENTPARSE,SCRIPTHELPERARRAY,
		NOTONEMAINLABEL,MONGOMANAGER,
		NOTYETIMPLEMENTED,FAILEDTEST,NOMEMORIZEDARG,
		FLAGPARSEREXCEPTION,CANNOTDOTASK,SCRIPTAPPEXCEPTION, MONGOUTIL
		,MISCUTILMANAGER
		,SIMPLEPARSER
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
