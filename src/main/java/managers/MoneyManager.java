package managers;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;

import assistantbot.MyAssistantUserData;
import util.MyBasicBot;
import util.parsers.StandardParser;
import util.parsers.StandardParser.ArgTypes;

public class MoneyManager implements managers.MyManager,OptionReplier{
	JSONArray cats = new JSONArray();
	MyAssistantUserData ud_ = null;
	MongoCollection<Document> money;
	public MoneyManager(MyAssistantUserData myAssistantUserData)
	{
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
	public String putMoney(JSONObject obj)
	{
		int msgid = ud_.sendMessageWithKeyBoard("which category?", cats);
		this.pendingOperations.put(msgid, obj);
		return String.format("prepare to put %s",obj.toString());
	}
	private void putMoney(JSONObject obj,String categoryName)
	{
//		int amount = ;
		Document res = new Document();
		res.put("amount", obj.getInt("amount"));
		res.put("category", categoryName);
		res.put("date", new Date());
		res.put("comment", obj.optString("comment"));
		money.insertOne(res);
	}
	public String getLastCosts(int howMuch)
	{
		final util.TableBuilder tb = new util.TableBuilder();
		tb.newRow();
		tb.addToken("amount");
		tb.addToken("category");
		tb.addToken("date");
		final Hashtable<String,Integer> totals = new Hashtable<String,Integer>();
		final util.TableBuilder tb1 = new util.TableBuilder();
		
		Block<Document> printBlock = new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   	JSONObject obj = new JSONObject(doc.toJson());
		    	   	String category = obj.getString("category");
					int amount = obj.getInt("amount");
					tb.newRow();
					tb.addToken(amount);
					if(!totals.containsKey(category))
						totals.put(category, 0);
					totals.put(category, totals.get(category) + amount);
					tb.addToken(category);
					tb.addToken(doc.getDate("date").toString());
		       }
		};
		money.find().sort(Sorts.descending("date")).limit(howMuch).forEach(printBlock);
		
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
	public String getMoneyCats()
	{
		util.TableBuilder tb = new util.TableBuilder();
		tb.newRow();
		tb.addToken("name");
		for(int i = 0; i < cats.length(); i++)
		{
			tb.newRow();
			tb.addToken(cats.getString(i));
		}
		
		return tb.toString();
	}
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("moneycats")==0) 
				return getMoneyCats();
			if(res.getString("name").equals("money")) {
				return putMoney(res);
			}
			if(res.getString("name").equals("costs"))
				return getLastCosts(res.getInt("num"));
		}
		return null;
	}
	@Override
	public JSONArray getCommands() {
		JSONArray res = new JSONArray(
				"["+ 
				"{\"name\":\"costs\",\"args\":[{\"name\":\"num\",\"type\":\"int\",\"isOpt\":true}]}]");
		res.put(AbstractManager.makeCommand("money", "spent money", 
				Arrays.asList(AbstractManager.makeCommandArg("amount",StandardParser.ArgTypes.integer, false),
						AbstractManager.makeCommandArg("comment", ArgTypes.remainder, true))));
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