package managers;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;

import assistantbot.ResourceProvider;
import util.AssistantBotException;
import util.Util;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class MoneyManager extends AbstractManager implements OptionReplier{
	JSONArray cats = new JSONArray();
	ResourceProvider ud_ = null;
	MongoCollection<Document> money;
	private static final String PATTERN = "yyyyMMddHHmm";
	public MoneyManager(ResourceProvider myAssistantUserData) throws Exception
	{
		super(GetCommands());
		MongoClient mongoClient = myAssistantUserData.getMongoClient();
		Block<Document> printBlock = new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   JSONObject obj = new JSONObject(doc.toJson());
		    	   cats.put(obj.getString("name"));
		       }
		};
		mongoClient.getDatabase("logistics").getCollection("moneycats").find().forEach(printBlock);
		money = mongoClient.getDatabase("logistics").getCollection("money");
		ud_ = myAssistantUserData;
	}
	Hashtable<Integer,JSONObject> pendingOperations = new Hashtable<Integer,JSONObject>();
	public String money(JSONObject obj) throws ParseException, JSONException, ScriptException, AssistantBotException
	{
		int msgid = ud_.sendMessageWithKeyBoard("which category?", cats);
		obj.put("amount", Util.SimpleEval(obj.getString("amount")));
		
		String comment = obj.optString("comment");
		System.err.format("comment=\"%s\"\n", comment);
		if(comment.matches("^"+StringUtils.repeat("[0-9]",PATTERN.length())+".*")) {
			System.err.format("%s matches!\n", comment);
			SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
			Date d = sdf.parse(comment.substring(0,PATTERN.length()));
			obj.put("date", d);
			obj.put("comment", comment.substring(PATTERN.length()).trim());
		}
		else
			System.err.format("%s doesn't match!\n",comment);
		
		this.pendingOperations.put(msgid, obj);
		return String.format("prepare to put %s",obj.toString());
	}
	private void putMoney(JSONObject obj,String categoryName)
	{
		Document res = new Document();
		res.put("amount", obj.getInt("amount"));
		res.put("category", categoryName);
		if(obj.has("date"))
			res.put("date", (Date)obj.get("date"));
		else
			res.put("date", new Date());
		res.put("comment", obj.optString("comment"));
		money.insertOne(res);
	}
	public String costs(JSONObject obj) {
		int howMuch = obj.getInt("num");
		final com.github.nailbiter.util.TableBuilder tb = new com.github.nailbiter.util.TableBuilder();
		tb.newRow();
		tb.addToken("#");
		tb.addToken("amount");
		tb.addToken("category");
		tb.addToken("date");
		final Hashtable<String,Integer> totals = new Hashtable<String,Integer>();
		final com.github.nailbiter.util.TableBuilder tb1 = new com.github.nailbiter.util.TableBuilder();
		
		final JSONObject container = new JSONObject().put("i", 1);
		money.find().sort(Sorts.descending("date")).limit(howMuch).forEach(new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   	JSONObject obj = new JSONObject(doc.toJson());
		    	   	String category = obj.getString("category");
					int amount = obj.getInt("amount");
					tb.newRow();
					tb.addToken(container.getInt("i"));
					container.put("i", container.getInt("i")+1);
					tb.addToken(amount);
					if(!totals.containsKey(category))
						totals.put(category, 0);
					totals.put(category, totals.get(category) + amount);
					tb.addToken(category);
					tb.addToken(doc.getDate("date").toString());
		       }
		});
		
		tb1.newRow();
		tb1.addToken("category");
		tb1.addToken("total");
		Set<String> keys = totals.keySet();
		Iterator<String> itr = keys.iterator();
	    while (itr.hasNext()) { 
	       String key = itr.next();
	       tb1.newRow();
	       tb1.addToken(key);
	       tb1.addToken(totals.get(key));
	    }
		
		return tb.toString()+"\n------------------\n"+tb1.toString();
	}
	public static JSONArray GetCommands() throws Exception {
		JSONArray res = new JSONArray()
				.put(new ParseOrderedCmd("costs","show last NUM costs",
						Arrays.asList((JSONObject) new ParseOrderedArg("num", ArgTypes.integer).makeOpt().useDefault(5))))
				.put(new ParseOrderedCmd("money", "spent money",
						Arrays.asList((JSONObject)new ParseOrderedArg("amount",ParseOrdered.ArgTypes.string),
								(JSONObject)new ParseOrderedArg("comment", ParseOrdered.ArgTypes.remainder).makeOpt())))
				;
		return res;
	}
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
	@Override
	public String optionReply(String option, Integer msgID) {
		if(this.pendingOperations.containsKey(msgID))
		{
			this.putMoney(this.pendingOperations.get(msgID), option);
			String res = String.format("put %s in category %s",
					this.pendingOperations.get(msgID).toString(),option);
			this.pendingOperations.remove(msgID);
			return res;
		}
		else
			return null;
	}
}