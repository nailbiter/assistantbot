package testing;

import java.util.HashMap;
import java.util.HashSet;

import util.AssistantBotException;
import util.parsers.ParseCommentLine;

class ParseCommentLineTest extends Test {
	private HashSet<String> tags_;
	private String rem_;
	ParseCommentLineTest() {
		tags_ = new HashSet<String>();
		tags_.add("tag1");
		tags_.add("tag2");
		rem_ = "test test test";
	}
	@Override
	public void test() throws AssistantBotException {
		test1();
		test2();
	}
	public void test1() throws AssistantBotException {
		ParseCommentLine pcl = new ParseCommentLine(ParseCommentLine.Mode.FROMLEFT);
		String test = rem_;
		for(String tag:tags_)
			test = String.format("#%s ", tag) + test;
		test = "%2200 " + test;
		HashMap<String, Object> res = pcl.parse(test);
		
		CheckEq(res.get(ParseCommentLine.REM),rem_);
		CheckEq(tags_,res.get(ParseCommentLine.TAGS));
	}
	public void test2() throws AssistantBotException {
		ParseCommentLine pcl = new ParseCommentLine(ParseCommentLine.Mode.FROMRIGHT);
		String test = rem_;
		for(String tag:tags_)
			test += String.format(" #%s", tag);
		test += " %2000";
		HashMap<String, Object> res = pcl.parse(test);
		
		CheckEq(res.get(ParseCommentLine.REM),rem_);
		CheckEq(tags_,res.get(ParseCommentLine.TAGS));
	}
}
