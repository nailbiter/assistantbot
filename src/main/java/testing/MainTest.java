package testing;

import java.util.ArrayList;

import util.AssistantBotException;

public class MainTest extends Test{
	private ArrayList<Test> tests_ = new ArrayList<Test>();
	
	public MainTest() {
		tests_.add(new ParseCommentLineTest());
	}
	@Override
	public void test() throws AssistantBotException {
		for(Test test:tests_)
			test.test();
	}
}
