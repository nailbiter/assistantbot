package managers;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import assistantbot.ResourceProvider;
import util.ArithmeticExpressionParser;
import util.AssistantBotException;
import util.ParseCommentLine;
import util.UserCollection;
import util.Util;
import util.parsers.FlagParser;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class MoneyManager extends AbstractManager{
	private static final String CATEGORIES = "categories";
	private static final String DECSIGNS = "decsigns";
	private static final String AMOUNT = "amount";
	private static final String COMMENT = "comment";
	HashSet<String> cats_ = new HashSet<String>();
	ResourceProvider rp_ = null;
	MongoCollection<Document> money;
	private String oldRemoveCostsValue_;
	
	public MoneyManager(ResourceProvider rp) throws Exception
	{
		super(GetCommands());
		for(Object o:this.getParamObject(rp).getJSONArray(CATEGORIES)) {
			cats_.add((String) o);
		}
		money = rp.getCollection(UserCollection.MONEY);
		rp_ = rp;
	}
	public String money(JSONArray array) throws Exception
	{
		for(Object o:array) {
			JSONObject obj = (JSONObject)o;
			if( !obj.has("amount") ) {
				if( array.length()==1 )
					return ShowTags(money,rp_);
				else
					continue;
			}
			
			if( obj.getString(AMOUNT).toLowerCase().equals("r") || obj.getString(AMOUNT).toLowerCase().equals("у") ) {
				return removeCosts(obj.optString(COMMENT, ""));
			}
			
			double am = new ArithmeticExpressionParser(getParamObject(rp_).getInt(DECSIGNS))
					.simpleEvalDouble(obj.getString("amount"));
			
			if( am < 0 ) {
				if( array.length()==1 )
					return showCosts( (int)-am ,obj.optString("comment", ""));
				else continue;
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
				if(cats_.contains(tag))
					category = tag;
			tags.removeAll(cats_);
			obj.put("tags", new JSONArray(tags));
			
			System.err.format("obj=%s\n", obj.toString(2));
			if( category != null ) {
				rp_.sendMessage(putMoney(obj,category));
			} else if( cats_.size()==1 ) {
				rp_.sendMessage(putMoney(obj, cats_.iterator().next()));
			} else {
				rp_.sendMessageWithKeyBoard("which category?", Util.AppendObjectMap(new JSONArray(cats_),obj), 
						new Transformer<Object,String>(){
							@Override
							public String transform(Object arg0) {
								ImmutablePair<String,Object> pair = (ImmutablePair<String, Object>) arg0;
								return putMoney((JSONObject) pair.right,pair.left);
							}
				});
				rp_.sendMessage(String.format("prepare to put %s",obj.toString()));
			}
		}
		return "";
	}
	private String removeCosts(String optString) {
		if(optString.isEmpty())
			optString = oldRemoveCostsValue_;
		oldRemoveCostsValue_ = optString;
		
		int pos = -1;
		final String SEPARATOR = "-";
		if( (pos = optString.indexOf(SEPARATOR))>=0 ) {
			String split1 = optString.substring(0, pos),
					split2 = optString.substring(pos+SEPARATOR.length());
			return removeCosts(Integer.parseInt(split1),Integer.parseInt(split2)+1);
		} else {
			int x = Integer.parseInt(optString);
			return removeCosts(x,x+1);
		}
	}
	private String removeCosts(final int from, final int till) {
		final HashSet<ObjectId> ids = new HashSet<ObjectId>();
		final JSONObject obj = new JSONObject()
				.put("i", 0);
		money
		.find()
		.sort(Sorts.descending("date"))
		.forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				int i = obj.getInt("i")+1;
				obj.put("i", i);
				if(from<=i && i<till) {
					ObjectId id = arg0.getObjectId("_id");
					ids.add(id);
					System.err.format("adding id \"%s\" to deletion\n", id);
				}
			}
		});
		
		StringBuilder sb = new StringBuilder();
		for(ObjectId id:ids) {
			sb.append(String.format("%s\n", id));
			money.deleteOne(Filters.eq("_id", id));
		}
//		rp_.sendMessage(sb.toString());
		return String.format("removed costs #%d..#%d", from,till-1);
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
		res.put("amount", obj.getDouble("amount"));
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
	private String showCosts(int howMuch, String flags) throws JSONException, Exception {
		final FlagParser fp = new FlagParser()
				.addFlag('c', "show comments")
				.parse(flags);
		final TableBuilder tb = new TableBuilder();
		tb.newRow()
		.addToken("#")
		.addToken("amount")
		.addToken("category")
		.addToken("date");
		if( fp.contains('c') )
			tb.addToken("comment");
		final Hashtable<String,Double> totals = new Hashtable<String,Double>();
		final TableBuilder tb1 = new TableBuilder();
		final String PRINTFLINE = String.format("%%.%df", getParamObject(rp_).getInt(DECSIGNS));
		
		final JSONObject container = new JSONObject().put("i", 1);
		money
		.find()
		.sort(Sorts.descending("date")).limit(howMuch).forEach(new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   	JSONObject obj = new JSONObject(doc.toJson());
		    	   	String category = obj.getString("category");
					double amount = obj.getDouble("amount");
					tb.newRow();
					tb.addToken(container.getInt("i"));
					container.put("i", container.getInt("i")+1);
					tb.addToken(String.format(PRINTFLINE, amount));
					if(!totals.containsKey(category))
						totals.put(category, 0.0);
					totals.put(category, totals.get(category) + amount);
					tb.addToken(category);
					DateFormat formatter = 
							new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
					String timezonename = 
							rp_.getUserObject().getString("timezone");
					formatter
						.setTimeZone(TimeZone.getTimeZone(timezonename));
					Date date = doc.getDate("date");
					tb.addToken(String.format("%s %s", formatter.format(date)
							,timezonename ));
					if( fp.contains('c') )
						tb.addToken(obj.optString("comment",""));
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
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
	@Override
	public void set() {
		final String NEWCATEGORY = "new category";
		final String REMOVECATEGORY = "remove category";
		final String CHOOSETOREMOVE = "choose category to remove";
		
		rp_.sendMessageWithKeyBoard("choose the setting:", Util.IdentityMap(new JSONArray()
				.put(NEWCATEGORY)
				.put(REMOVECATEGORY)), new Transformer<Object,String>(){
			@Override
			public String transform(Object arg0) {
				String cmd = (String) arg0;
				if(cmd.equals(NEWCATEGORY)) {
					try {
						rp_.sendMessage("reply to this message with a name of new category", 
								new Transformer<String,String>(){
							@Override
							public String transform(String arg0) {
								return addCategory(arg0);
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
						return String.format("%s e:%s", NEWCATEGORY,e.getMessage());
					}
					return NEWCATEGORY;
				} else if(cmd.equalsIgnoreCase(REMOVECATEGORY)) {
					rp_.sendMessageWithKeyBoard(CHOOSETOREMOVE, Util.IdentityMap(new JSONArray(cats_)),
							new Transformer<Object,String>(){
								@Override
								public String transform(Object arg0) {
									return removeCategory((String) arg0);
								}
					});
					return CHOOSETOREMOVE;
				} else {
					return null;
				}
			}
		});
	}
	private String removeCategory(String catname) {
		cats_.remove(catname);
		rp_.setManagerSettingsObject(this.getClass().getName(), CATEGORIES, new JSONArray(cats_));
		return String.format("removed category \"%s\"", catname);
	}
	private String addCategory(String catname) {
		cats_.add(catname);
		rp_.setManagerSettingsObject(this.getClass().getName(), CATEGORIES, new JSONArray(cats_));
		return String.format("added category \"%s\"", catname);
	}
}