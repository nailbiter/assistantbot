package managers;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.script.ScriptException;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import assistantbot.ResourceProvider;
import util.AssistantBotException;
import util.ParseCommentLine;
import util.UserCollection;
import util.Util;
import util.db.MongoUtil;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class MoneyManager extends AbstractManager implements OptionReplier{
	private static final String USERNAME = "username";
	HashSet<String> cats = new HashSet<String>();
	ResourceProvider rp_ = null;
	MongoCollection<Document> money;
	Hashtable<Integer,JSONObject> pendingOperations = new Hashtable<Integer,JSONObject>();
	
	public MoneyManager(ResourceProvider rp) throws Exception
	{
		super(GetCommands());
		rp.getCollection(UserCollection.MONEYCATS)
			.find()
			.forEach(new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   cats.add(new JSONObject(doc.toJson()).getString("name"));
		       }
			});
		money = rp.getCollection(UserCollection.MONEY);
		rp_ = rp;
	}
	public String money(JSONObject obj) throws ParseException, JSONException, ScriptException, AssistantBotException
	{
		if( !obj.has("amount") ) {
			return ShowTags(money,rp_);
		}
		
		int am = 0;
		try {
//			Integer.parseInt(obj.getString("amount"));
			am = Util.SimpleEval(obj.getString("amount"));
		} catch(AssistantBotException e) {
		}
		if( am < 0 ) {
			return costs( -am );
		}
		
		obj.put("amount", am);
		
		String comment = obj.optString("comment");
		System.err.format("comment=\"%s\"\n", comment);
		HashMap<String, Object> parsed = new ParseCommentLine(ParseCommentLine.Mode.FROMLEFT).parse(comment);
		if( parsed.containsKey(ParseCommentLine.DATE) )
			obj.put("date", (Date)parsed.get(ParseCommentLine.DATE));
		obj.put("comment", (String)parsed.getOrDefault(ParseCommentLine.REM,""));
		
		Set<String> tags = (Set<String>)parsed.get(ParseCommentLine.TAGS);
		String category = null;
		for(String tag:tags)
			if(cats.contains(tag))
				category = tag;
		tags.removeAll(cats);
		obj.put("tags", new JSONArray(tags));
		
		if( category == null ) {
			int msgid = rp_.sendMessageWithKeyBoard("which category?", new JSONArray(cats));
			this.pendingOperations.put(msgid, obj);
			return String.format("prepare to put %s",obj.toString());
		} else {
			return putMoney(obj,category);
		}
	}
	private static String ShowTags(MongoCollection<Document> money, ResourceProvider rp) {
		final StringBuilder sb = new StringBuilder("tags: \n");
		final HashSet<String> set = new HashSet<String> ();
		money
		.find()
		.forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				JSONArray arr;
				try {
					arr = new JSONObject(arg0.toJson()).getJSONArray("tags");
				} catch(JSONException e) {
					return;
				}
				for(Object o:arr)
					set.add((String)o);
			}
		});
		
		ArrayList<String> rres = new ArrayList<String>(set);
		Collections.sort(rres);
		for(String tag:rres)
			sb.append(String.format("%s%s\n", "  ",tag));
		return sb.toString();
	}
	private String putMoney(JSONObject obj,String categoryName)
	{
		Document res = new Document();
		res.put("amount", obj.getInt("amount"));
		res.put("category", categoryName);
		if(obj.has("date"))
			res.put("date", (Date)obj.get("date"));
		else
			res.put("date", new Date());
		res.put("comment", obj.getString("comment"));
		res.put("tags", obj.getJSONArray("tags"));
		money.insertOne(res);
		return String.format("put %s in category %s",
					obj.toString(),categoryName);
	}
	private String costs(int howMuch) {
		final com.github.nailbiter.util.TableBuilder tb = new com.github.nailbiter.util.TableBuilder();
		tb.newRow();
		tb.addToken("#");
		tb.addToken("amount");
		tb.addToken("category");
		tb.addToken("date");
		final Hashtable<String,Integer> totals = new Hashtable<String,Integer>();
		final com.github.nailbiter.util.TableBuilder tb1 = new com.github.nailbiter.util.TableBuilder();
		
		final JSONObject container = new JSONObject().put("i", 1);
		money
		.find()
		.sort(Sorts.descending("date")).limit(howMuch).forEach(new Block<Document>() {
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
		return new JSONArray()
				.put(new ParseOrderedCmd("money", "spent money",
						new ParseOrderedArg("amount",
								ParseOrdered.ArgTypes.string)
						.makeOpt(),
						new ParseOrderedArg("comment", 
								ParseOrdered.ArgTypes.remainder)
						.makeOpt())
						.makeDefaultHandler())
				;
	}
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
	@Override
	public String optionReply(String option, Integer msgID) {
		if(this.pendingOperations.containsKey(msgID))
		{
			String res = this.putMoney(this.pendingOperations.get(msgID), option);
			this.pendingOperations.remove(msgID);
			return res;
		}
		else
			return null;
	}
}