/**
 * 
 */
package managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Timer;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.MyAssistantUserData;
import it.sauronsoftware.cron4j.Scheduler;
import managers.tests.ParadigmTest;
import util.MyBasicBot;
import util.StorageManager;
import util.TableBuilder;
import util.parsers.StandardParser;
import static managers.AbstractManager.makeCommand;
import static managers.AbstractManager.makeCommandArg;

/**
 * @author nailbiter
 */
public class TestManager extends AbstractManager implements OptionReplier {
	Long chatID_ = null;
	Scheduler scheduler_ = null;
	MyBasicBot bot_ = null;
	private Logger logger_ = null;
	MyAssistantUserData ud_ = null;
	Timer timer_ = null;
	ParadigmTest paradigmtest_ = null;
	Random rand = new Random();
	public TestManager(Long chatID, MyBasicBot bot, Scheduler scheduler, MyAssistantUserData myAssistantUserData) throws Exception{
		ud_ = myAssistantUserData;
		chatID_ = chatID;
		bot_ = bot;
		scheduler_ = scheduler;
		logger_ = Logger.getLogger(this.getClass().getName());
		timer_ = new Timer();
		
		addParadigmTest();
	}
	private void addParadigmTest() throws Exception
	{
		paradigmtest_ = new ParadigmTest(bot_);
	}

	/* (non-Javadoc)
	 * @see util.MyManager#getCommands()
	 */
	@Override
	public JSONArray getCommands() {
		JSONArray res = new JSONArray();
		res.put(makeCommand("tests","show tests, -1 reloads",
				Arrays.asList(makeCommandArg("index", StandardParser.ArgTypes.integer, true))));
		res.put(makeCommand("testsetscore","set test score",
				Arrays.asList(
						makeCommandArg("mode",StandardParser.ArgTypes.string,true),
						makeCommandArg("score",StandardParser.ArgTypes.string,true),
						makeCommandArg("testnum",StandardParser.ArgTypes.integer,true))));
		res.put(makeCommand("testdo","paradigm test done",
				Arrays.asList(makeCommandArg("index", StandardParser.ArgTypes.integer, false))));
		return res;
	}
	public String testsetscore(JSONObject obj) throws Exception{
		
		return obj.toString();
	}
	public String tests(JSONObject obj) throws Exception
	{
		if(!obj.has("index")) {
			TableBuilder tb = new TableBuilder();
			tb.addNewlineAndTokens("#", "name","layout");
			for(int i = 1; i <= paradigmtest_.getSize(); i++)
				tb.addNewlineAndTokens(Integer.toString(i), 
						paradigmtest_.getTestName(i),
						String.format("%dx%d", paradigmtest_.getRowNum(i),paradigmtest_.getColNum(i)));
			return tb.toString();
		} else if(obj.getInt("index")>0) {
			return paradigmtest_.showTest(obj.getInt("index"));
		}else {
//			throw new Exception("index<0");
			paradigmtest_ = new ParadigmTest(bot_);
			return "tests reloaded";
		}
			
	}
	public String testdo(JSONObject obj) throws Exception
	{
		int index = obj.getInt("index");
		String[] res = this.paradigmtest_.isCalled(index);
		logger_.info(String.format("run this index=%d", index));
		int id = -1;
		if(res==null || res.length==0)
			logger_.info("bad");
		if(res.length == 1)
		{
			id = bot_.sendMessage(res[0], chatID_,this);
			logger_.info(String.format("index=%d", id));
			this.waitingForReply.put(id, index);
		}
		else
			sendMessageWithKeyBoard(res[0], Arrays.copyOfRange(res, 1, res.length));
		return res.toString();
	}

	@Override
	public String processReply(int messageID,String msg) {
		logger_.info(String.format("messageID=%d", messageID));
		
		Integer index = this.waitingForReply.get(messageID);
		logger_.info(String.format("index=%d", index));
		if(index == null)
			return null;
		return this.paradigmtest_.processReply(msg, index);
	}
	Hashtable<Integer,Integer> waitingForReply = new Hashtable<Integer,Integer>();
	@Override
	public String optionReply(String option, Integer msgID) {
		return null;
	}
	public int sendMessageWithKeyBoard(String msg, String[] categories)
	{
		JSONArray c = new JSONArray();
		for(int i = 0; i < categories.length; i++)
			c.put(categories[i]);
		return ud_.sendMessageWithKeyBoard(msg, c);
	}
}
