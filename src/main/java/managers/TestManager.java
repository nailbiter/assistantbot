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
		
		JSONObject obj = StorageManager.get("tests", false),
				objData = StorageManager.get("testsData", true);
		addTest("paradigm",obj,objData);
		//addTest("genders",obj,objData);
		//addTest("plural",obj,objData);
	}
	private void addTest(String name,JSONObject obj, JSONObject objData) throws Exception
	{
		if(!objData.has(name))
			objData.put(name, new JSONObject());
		
		System.out.println(String.format("objData=%s, obj=%s", objData.toString(),obj.toString()));
		
		if(name.equals("paradigm"))
			paradigmtest_ = new ParadigmTest(obj.getJSONObject(name),objData.getJSONObject(name),this,name,timer_);
		/*if(name.equals("plural"))
			tests.add(new PluralTest(obj.getJSONObject(name),objData.getJSONObject(name),this,name,timer_));
		if(name.equals("genders"))
			tests.add(new ChoiceTest(obj.getJSONObject(name),objData.getJSONObject(name),this,name,timer_));*/
	}

	/* (non-Javadoc)
	 * @see util.MyManager#getCommands()
	 */
	@Override
	public JSONArray getCommands() {
		JSONArray res = new JSONArray();
		res.put(AbstractManager.makeCommand("tests","show tests",new ArrayList<JSONObject>()));
		res.put(AbstractManager.makeCommand("testrand","show random test",new ArrayList<JSONObject>()));
		res.put(makeCommand("testdo","paradigm test done",
				Arrays.asList(makeCommandArg("index", StandardParser.ArgTypes.integer, false))));
		return res;
	}
	public String testrand(JSONObject obj) throws Exception
	{
		JSONObject o = new JSONObject()
				.put("index",rand.nextInt(paradigmtest_.getSize()));
		return testdo(o);
	}
	public String tests(JSONObject obj) throws Exception
	{
//		return String.format("# of paradigm tests: %d", paradigmtest_.getSize());
		TableBuilder tb = new TableBuilder();
		tb.addNewlineAndTokens("#", "name","layout");
		for(int i = 0; i < paradigmtest_.getSize(); i++)
			tb.addNewlineAndTokens(Integer.toString(i), 
					paradigmtest_.getTestName(i),
					String.format("%dx%d", paradigmtest_.getRowNum(i),paradigmtest_.getColNum(i)));
		return tb.toString();
	}
	public String testdo(JSONObject obj) throws Exception
	{
		int index = obj.getInt("index") - 1;
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
