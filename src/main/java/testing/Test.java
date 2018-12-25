package testing;

import util.AssistantBotException;

public abstract class Test {
	protected void FailTest(String msg) throws AssistantBotException {
		throw new AssistantBotException(AssistantBotException.Type.FAILEDTEST, msg);
	}
	protected void CheckEq(Object o1,Object o2) throws AssistantBotException {
		if( !o1.equals(o2) )
			FailTest(String.format("\"%s\"!=\"%s\"", o1.toString(),o2.toString()));
	}
	abstract public void test() throws AssistantBotException ;
}
