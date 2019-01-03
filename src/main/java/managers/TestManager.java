/**
 * 
 */
package managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Timer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.Transformer;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import assistantbot.ResourceProvider;
import managers.tests.JsonTest;
import managers.tests.ParadigmTest;
import managers.tests.UrlTest;
import util.MongoUtil;
import util.UserCollection;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;
import static util.parsers.ParseOrdered.ArgTypes;
import static java.util.Arrays.asList;

/**
 * @author nailbiter
 */
public class TestManager extends AbstractManager implements OptionReplier {
	Long chatID_ = null;
	private Logger logger_ = null;
	ResourceProvider rp_ = null;
	Timer timer_ = null;
	ArrayList<JsonTest> testContainer_ = new ArrayList<JsonTest>();
	Random rand = new Random();
	private MongoCollection<Document> testScores_;
	int lastUsedTestIndex = -1;
	public TestManager(ResourceProvider rp) throws Exception{
		super(GetCommands());
		rp_ = rp;
		logger_ = Logger.getLogger(this.getClass().getName());
		timer_ = new Timer();
		AddTests(testContainer_,rp_);
		testScores_ = rp_
//				.getMongoClient()
//				.getDatabase(MongoUtil.LOGISTICS)
//				.getCollection("scoresOfTests");
				.getCollection(UserCollection.SCORESOFTESTS);
	}
	private static void AddTests(ArrayList<JsonTest> testContainer, ResourceProvider rp) throws Exception {
		testContainer.clear();
		ParadigmTest.AddTests(testContainer,rp);
		UrlTest.AddTests(testContainer,rp);
		
		HashSet<String> names = new HashSet<String>();
		for(JsonTest t:testContainer)
			names.add(t.getName());
		if(names.size()!=testContainer.size())
			throw new Exception(String.format("testmanager size: %d<%d", names.size(),testContainer.size()));
	}
	public static JSONArray GetCommands() throws Exception {
		JSONArray res = new JSONArray()
				.put(new ParseOrderedCmd("tests","show tests, -1 reloads",
						asList(new ParseOrderedArg("index", ArgTypes.integer).makeOpt().j())))
				.put(new ParseOrderedCmd("testsetscore","set test score, MODE=s|u, score=15/19",
						asList(new ParseOrderedArg("score",ArgTypes.string).makeOpt().j(),
								new ParseOrderedArg("testnum",ArgTypes.integer).makeOpt().j())))
				.put(new ParseOrderedCmd("testdo","paradigm test done",
						asList(new ParseOrderedArg("index", ArgTypes.integer)
								.makeOpt()
								.useMemory(new Transformer<Object,Object>(){
									@Override
									public Object transform(Object arg0) {
										return ((int)arg0)+1;
									}
								})
								.j())));
		return res;
	}
	public String testsetscore(JSONObject obj) throws Exception{
		if(!obj.has("testnum"))
			obj.put("testnum", this.lastUsedTestIndex);
		if(!obj.has("score"))
			obj.put("score", "1/1");
		
		obj.put("score", ScoreToDouble(obj.getString("score")));
		
		Document doc = new Document();
		doc.put("date", new Date());
		doc.put("testname", testContainer_.get(obj.getInt("testnum")).getName());
		doc.put("score", obj.getDouble("score"));
		testScores_.insertOne(doc);
		return String.format("put: %s to scores",new JSONObject(doc.toJson()).toString(2));
	}
	private static double ScoreToDouble(String score) throws Exception{
		Matcher m = null;
		if((m = Pattern.compile("\\s*(\\d+)\\s*/\\s*(\\d+)\\s*").matcher(score)).matches()) {
			return Double.parseDouble(m.group(1))/Double.parseDouble(m.group(2));
		} else if((m = Pattern.compile("\\s*(\\d+)\\s*%\\s*").matcher(score)).matches()) {
			return Integer.parseInt(m.group(1))/100.0;
		} else {
			throw new Exception(String.format("cannot parse %s", score));
		}
	}
	public String tests(JSONObject obj) throws Exception
	{
		if(!obj.has("index")) {
			TableBuilder tb = new TableBuilder();
			tb.addNewlineAndTokens("#", "description");
			for(int i = 1; i < testContainer_.size(); i++)
				tb.addNewlineAndTokens(Integer.toString(i),
						testContainer_.get(i).toString());
			return tb.toString();
		} else if(obj.getInt("index")<0) {
			AddTests(testContainer_,rp_);
			return String.format("%d tests loaded", testContainer_.size());
		} else {
			lastUsedTestIndex = obj.getInt("index");
			return testContainer_.get(lastUsedTestIndex).showTest();
		}
	}
	public String testdo(JSONObject obj) throws Exception
	{
		int index = lastUsedTestIndex = obj.getInt("index");
		rp_.sendMessage(String.format("test #%d", index));
		String[] res = this.testContainer_.get(index).isCalled();
		logger_.info(String.format("run this index=%d", index));
		int id = -1;
		if(res==null || res.length==0)
			logger_.info("bad");
		if(res.length == 1)
		{
			id = rp_.sendMessage(res[0], this);
			logger_.info(String.format("index=%d", id));
			this.waitingForReply.put(id, index);
		}
		else {
			sendMessageWithKeyBoard(res[0], Arrays.copyOfRange(res, 1, res.length));
		}
			
		return "";
	}

	@Override
	public String processReply(int messageID,String msg) {
		logger_.info(String.format("messageID=%d", messageID));
		
		Integer index = this.waitingForReply.get(messageID);
		logger_.info(String.format("index=%d", index));
		if(index == null)
			return null;
		this.lastUsedTestIndex = index;
		return testContainer_.get(lastUsedTestIndex).processReply(msg);
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
		return rp_.sendMessageWithKeyBoard(msg, c);
	}
}
