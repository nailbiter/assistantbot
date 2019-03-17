package managers;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.mongodb.Block;
import com.mongodb.client.model.Sorts;

import assistantbot.ResourceProvider;
import managers.money.MoneyManagerBase;
import util.Message;
import util.Util;
import util.parsers.FlagParser;
import util.parsers.ParseCommentLine;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class MoneyManager extends MoneyManagerBase{
	public MoneyManager(ResourceProvider rp) throws Exception {
		super(GetCommands(),rp);
	}
	public String money(JSONArray array) throws Exception
	{
		for(Object o:array) {
			JSONObject obj = (JSONObject)o;
			if( !obj.has("amount") ) {
				if( array.length()==1 )
					return ShowTags(money_,rp_);
				else
					continue;
			}
			
			for(String res = checkOperation(obj.getString(AMOUNT),obj.optString(COMMENT, ""));;) {
				if( res != null )
					rp_.sendMessage(new Message(res));
				break;
			}
			
			double am = StringToAm(obj.getString("amount"),getParamObject(rp_),money_);
			
			if( am < 0 ) {
				if( array.length()==1 )
					return showCosts( (int)-am ,obj.optString("comment", ""));
				else 
					continue;
			}
			
			obj.put("amount", am);
			String comment = obj.optString("comment");
			System.err.format("comment=\"%s\"\n", comment);
			HashMap<String, Object> parsed = new ParseCommentLine(ParseCommentLine.Mode.FROMLEFT)
					.parse(comment);
			if( parsed.containsKey(ParseCommentLine.DATE) )
				obj.put("date", (Date)parsed.get(ParseCommentLine.DATE));
			obj.put("comment", (String)parsed.getOrDefault(ParseCommentLine.REM,""));
			
			Set<String> tags = (Set<String>)parsed.get(ParseCommentLine.TAGS);
			String category = null;
			for(String tag:tags)
				if(cats_.contains(tag))
					category = tag;
			tags.removeAll(cats_);
			obj.put("tags", new JSONArray(tags));
			
			System.err.format("obj=%s\n", obj.toString(2));
			if( category != null ) {
				rp_.sendMessage(new Message(putMoney(obj,category)));
			} else if( cats_.size()==1 ) {
				rp_.sendMessage(new Message(putMoney(obj, cats_.iterator().next())));
			} else {
				rp_.sendMessageWithKeyBoard(new Message("which category?"), Util.AppendObjectMap(new JSONArray(cats_),obj), 
						new Transformer<Object,Message>(){
							@Override
							public Message transform(Object arg0) {
								ImmutablePair<String,Object> pair = (ImmutablePair<String, Object>) arg0;
								return new Message(putMoney((JSONObject) pair.right,pair.left));
							}
				});
				rp_.sendMessage(new Message(String.format("prepare to put %s",obj.toString())));
			}
		}
		return "";
	}
	private String putMoney(JSONObject obj,String categoryName)
	{
		Document res = new Document();
		res.put("amount", obj.getDouble("amount"));
		res.put("category", categoryName);
		if(obj.has("date"))
			res.put("date", (Date)obj.get("date"));
		else
			res.put("date", new Date());
		res.put("comment", obj.getString("comment"));
		res.put("tags", obj.getJSONArray("tags"));
		money_.insertOne(res);
		return String.format("put %s in category %s",
					obj.toString(),categoryName);
	}
	private String showCosts(int howMuch, String comment) throws JSONException, Exception {
		final String PREDICATE = "predicate";
		final HashMap<String, Object> parsed = new ParseCommentLine(ParseCommentLine.Mode.FROMLEFT)
				.addHandler(PREDICATE, "j:", ParseCommentLine.TOKENTYPE.STRING)
				.parse(comment);
		fp_.parse((String) parsed.getOrDefault(ParseCommentLine.REM,""));
		if(fp_.contains('h'))
			return fp_.getHelp();
		
		ScriptEngineManager factory = new ScriptEngineManager();
		final ScriptEngine engine = factory.getEngineByName("JavaScript");
		
		final TableBuilder tb = new TableBuilder();
		tb.newRow()
			.addToken("#")
			.addToken("amount")
			.addToken("category")
			.addToken("date");
		if( fp_.contains('c') )
			tb.addToken("comment");
		if( fp_.contains('t') )
			tb.addToken("tags");
		final Hashtable<String,Double> totals = new Hashtable<String,Double>();
		final TableBuilder tb1 = new TableBuilder();
		final String PRINTFLINE = String.format("%%.%df", getParamObject(rp_).getInt(DECSIGNS));
		
		final MutableInt i = new MutableInt(0);
		money_
		.find()
		.sort(Sorts.descending("date")).limit(howMuch).forEach(new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   	JSONObject obj = new JSONObject(doc.toJson());
		    	   	if( !filter(obj) ) {
		    	   		return;
		    	   	}
		    	   	String category = obj.getString("category");
					double amount = obj.getDouble("amount");
					tb.newRow();
					tb.addToken(i.addAndGet(1));
					tb.addToken(String.format(PRINTFLINE, amount));
					if(!totals.containsKey(category))
						totals.put(category, 0.0);
					totals.put(category, totals.get(category) + amount);
					tb.addToken(category);
					DateFormat formatter = 
							new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
					String timezonename = GetTimeZone(rp_);
					formatter
						.setTimeZone(TimeZone.getTimeZone(timezonename));
					Date date = doc.getDate("date");
					tb.addToken(String.format("%s %s", formatter.format(date)
							,timezonename));
					if( fp_.contains('c') )
						tb.addToken(obj.optString("comment",""));
					if( fp_.contains('t') ) 
						tb.addToken(String.join(", "
								, Util.Map((obj.has("tags")?obj.getJSONArray("tags"):new JSONArray()).toList()
										,new Transformer<Object,String>(){
											@Override
											public String transform(Object arg0) {
												return arg0.toString();
											}
								})));
		       }
			private boolean filter(JSONObject obj) {
				for(String tag:(Set<String>)parsed.get(ParseCommentLine.TAGS)) {
					if(!obj.has("tags") || ArrayUtils.indexOf(obj.getJSONArray("tags").toList().toArray(), tag)==ArrayUtils.INDEX_NOT_FOUND)
						return false;
				}
				engine.put("x", obj.toString());
				for(String tag:(Set<String>)parsed.get(PREDICATE)) {
					System.err.format("java: \"%s\"\n", tag);
					boolean res = true;
					try {
						res = (boolean) engine.eval(tag);
					} catch (ScriptException e) {
						e.printStackTrace();
					}
					if( !res )
						return res; 
				}
				return true;
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
	       tb1.addToken(String.format(String.format("%%.%df", getParamObject(rp_).getInt(DECSIGNS)), 
	    		   totals.get(key)));
	    }
		
		return 
				tb
				+"------------------"
				+"\n"
				+tb1
				;
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
						.makeDefaultHandler()
						.makeMultiline()
						);
	}
	/**
	 * @deprecated use {@link WithSettingsManager}
	 */
	@Override
	public void set() {
		final String NEWCATEGORY = "new category";
		final String REMOVECATEGORY = "remove category";
		final String CHOOSETOREMOVE = "choose category to remove";
		
		rp_.sendMessageWithKeyBoard(new Message("choose the setting:"), Util.IdentityMap(new JSONArray()
				.put(NEWCATEGORY)
				.put(REMOVECATEGORY)), new Transformer<Object,Message>(){
			@Override
			public Message transform(Object arg0) {
				String cmd = (String) arg0;
				if(cmd.equals(NEWCATEGORY)) {
					try {
						rp_.sendMessage(new Message("reply to this message with a name of new category"), 
								new Transformer<String,Message>(){
							@Override
							public Message transform(String arg0) {
								return new Message(addCategory(arg0));
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
						return new Message(String.format("%s e:%s", NEWCATEGORY,e.getMessage()));
					}
					return new Message(NEWCATEGORY);
				} else if(cmd.equalsIgnoreCase(REMOVECATEGORY)) {
					rp_.sendMessageWithKeyBoard(new Message(CHOOSETOREMOVE), Util.IdentityMap(new JSONArray(cats_)),
							new Transformer<Object,Message>(){
								@Override
								public Message transform(Object arg0) {
									return new Message(removeCategory((String) arg0));
								}
					});
					return new Message(CHOOSETOREMOVE);
				} else {
					return null;
				}
			}
		});
	}
}