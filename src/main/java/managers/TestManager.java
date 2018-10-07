/**
 * 
 */
package managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Timer;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.client.MongoCollection;

import assistantbot.MyAssistantUserData;
import it.sauronsoftware.cron4j.Scheduler;
import managers.tests.ParadigmTest;
import util.MyBasicBot;
import util.StorageManager;
import util.TableBuilder;
import util.parsers.StandardParser;
import static managers.AbstractManager.MakeCommand;
import static managers.AbstractManager.MakeCommandArg;

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
	private MongoCollection<Document> testScores_;
	int lastUsedTestIndex = -1;
	public TestManager(Long chatID, MyBasicBot bot, Scheduler scheduler, MyAssistantUserData myAssistantUserData) throws Exception{
		ud_ = myAssistantUserData;
		chatID_ = chatID;
		bot_ = bot;
		scheduler_ = scheduler;
		logger_ = Logger.getLogger(this.getClass().getName());
		timer_ = new Timer();
		
		addParadigmTest();
		testScores_ = bot_.getMongoClient().getDatabase("logistics").getCollection("scoresOfTests");
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
		res.put(MakeCommand("tests","show tests, -1 reloads",
				Arrays.asList(MakeCommandArg("index", StandardParser.ArgTypes.integer, true))));
		res.put(MakeCommand("testsetscore","set test score, MODE=s|u, score=15/19",
				Arrays.asList(
						MakeCommandArg("score",StandardParser.ArgTypes.string,false),
						MakeCommandArg("testnum",StandardParser.ArgTypes.integer,true))));
		res.put(MakeCommand("testdo","paradigm test done",
				Arrays.asList(MakeCommandArg("index", StandardParser.ArgTypes.integer, false))));
		return res;
	}
	public String testsetscore(JSONObject obj) throws Exception{
		if(!obj.has("testnum"))
			obj.put("testnum", this.lastUsedTestIndex);
		
		String[] scoreParts = obj.getString("score").split("/");
		obj.put("score", Double.parseDouble(scoreParts[0].trim())/Double.parseDouble(scoreParts[1].trim()));
		
		Document doc = new Document();
		doc.put("date", new Date());
		doc.put("testindex", obj.getInt("testnum"));
		doc.put("score", obj.getDouble("score"));
		testScores_.insertOne(doc);
		return String.format("put %s to scores",obj.toString());
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
		} else {
			lastUsedTestIndex = obj.getInt("index");
			return paradigmtest_.showTest(lastUsedTestIndex);
		}
			
	}
	public String testdo(JSONObject obj) throws Exception
	{
		int index = lastUsedTestIndex = obj.getInt("index");
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
		this.lastUsedTestIndex = index;
		return this.paradigmtest_.processReply(msg, this.lastUsedTestIndex);
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
