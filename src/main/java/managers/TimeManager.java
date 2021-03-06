package managers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

import assistantbot.ResourceProvider;
import jshell.command.cat;
import util.Util;
import util.db.MongoUtil;
import util.AssistantBotException;
import util.JsonUtil;
import util.Message;
import util.UserCollection;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedCmd;

import static java.util.Arrays.asList;

/**
 * @author nailbiter
 *
 */
public class TimeManager extends AbstractManager implements Runnable, OptionReplier {
	private static final int DELAYMIN = 30;
	protected static int ROWNUM = 2;
	protected static final String NOWORKCATNAME = "useless";
	protected static final Message WHEREAREYOUNOW = new Message("北鼻，你在幹什麼？");
	private static final Date MYDEATHDATA_ = new Date(1991 + 80, 12, 24);
	protected JSONObject sleepingObj_ = null;
	JSONArray categories_;
	boolean isWaitingForAnswer_ = false;
	MongoCollection<Document> time_, sleepingTimes_;
	int waitingForTimeReportMessageId_ = -1;
	int waitingForPersistentCategoryChoiceMessageId_ = -1;
	private ResourceProvider rp_;
	
	public TimeManager(ResourceProvider rp) throws AssistantBotException {
		super(GetCommands());
		rp_ = rp;
		time_ = rp_.getCollection(UserCollection.TIME);
		categories_ = 
			MongoUtil.GetJSONArrayFromDatabase(rp_.getCollection(UserCollection.TIMECATS));
		for( int i = 0; i < categories_.length(); i++ ) {
			if( !categories_.getJSONObject(i).optBoolean("isTimeCat",true) ) {
				categories_.remove(i);
				i--;
			}
		}
		System.err.println("timecats");
		for( int i = 0; i < categories_.length(); i++ ) {
			System.err.format("\t%s\n",categories_.getJSONObject(i).getString("name"));
		}
		rp.getScheduler().schedule(String.format("*/%d * * * *",DELAYMIN), this);
		sleepingTimes_ = rp_.getCollection(UserCollection.SLEEPINGTIMES);
	}
	public String timestat(JSONObject res) {
		int num = res.optInt("num",48);
		System.out.println("got num="+num);
		
		final Hashtable<String,Integer> ht = new Hashtable<String,Integer>();
		Block<Document> printBlock = new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   JSONObject obj = new JSONObject(doc.toJson());
		    	   String cat = obj.getString("category");
		    	   if(!ht.containsKey(cat))
						ht.put(cat, 0);
					int res1 = ht.get(cat);
					ht.put(cat, res1+1);
		       }
		};
		
		time_.find().sort(Sorts.descending("date")).limit(num).forEach(printBlock);
		List<Map.Entry<String, Integer>> list = 
				new LinkedList<Map.Entry<String,Integer>>(ht.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String,Integer>>()
			{
				@Override
				public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
					return Integer.compare(o2.getValue(),o1.getValue());
				}
			});
		com.github.nailbiter.util.TableBuilder tb = new com.github.nailbiter.util.TableBuilder();
		for(int i = 0; i < list.size(); i++){
			tb.newRow();
			tb.addToken(list.get(i).getKey()+":");
			tb.addToken(printTime(list.get(i).getValue(),res.optString("key")));
		}
		
		return tb.toString();
	}
	protected static String printTime(int num, String key)
	{
		if(key.contains("n"))
			return String.format("%.1fh", num/2.0);
		else
			return StringUtils.repeat("*",num);
	}
	@Override
	public void run(){
		try{
			System.err.println("run this");
			
			boolean isSleeping = isSleeping();
			System.err.format("isSleeping=%s, wfa_=%s\n", isSleeping,isWaitingForAnswer_);
			if(isWaitingForAnswer_) {
				writeTimeEntry(NOWORKCATNAME);
				waitingForTimeReportMessageId_ = 
						rp_.sendMessageWithKeyBoard(WHEREAREYOUNOW, getCatNames());
			} else if(isSleeping) {
				String msg = gotUpdate(sleepingObj_.getString("name"));
				if(sleepingObj_.getString("canBePersistent").equals("message")) {
					waitingForTimeReportMessageId_ = 
							rp_.sendMessage(new Message(msg));
				}
			} else {
				waitingForTimeReportMessageId_ = 
						rp_.sendMessageWithKeyBoard(WHEREAREYOUNOW, getCatNames());
				isWaitingForAnswer_ = true;
			}
		}
		catch(Exception e) { e.printStackTrace(System.err); }
	}
	
	private JSONArray getCatNames() {
		JSONArray res = new JSONArray();
		for(Object cat:categories_)
			res.put(((JSONObject)cat).getString("name"));
		return res;
	}
	public String gotUpdate(String categoryName) throws Exception {
		writeTimeEntry(categoryName);
		this.isWaitingForAnswer_ = false;
		return "got: "+categoryName+"\n"+this.getLifetime();
	}
	private void writeTimeEntry(String categoryName) {
		Document res = new Document();
		res.put("date", new Date());
		res.put("category", categoryName);
		time_.insertOne(res);
	}
	protected String getLifetime()
	{
		Date currentData = new Date();
		return "remaining time to live: " + Util.milisToTimeFormat(MYDEATHDATA_.getTime() - currentData.getTime());
	}
	private static JSONArray GetCommands() throws AssistantBotException {
		JSONArray res = new JSONArray();
		res.put(ParseOrdered.MakeCommand("timestat", "statistics about time used", 
				asList(
						ParseOrdered.MakeCommandArg("num", ParseOrdered.ArgTypes.integer, true),
						ParseOrdered.MakeCommandArg("key", ParseOrdered.ArgTypes.string, true))));
		res.put(new ParseOrderedCmd("sleepstart","start sleeping", new ArrayList<JSONObject>()));
		res.put(ParseOrdered.MakeCommand("sleepend","end sleeping", new ArrayList<JSONObject>()));
		return res;
	}
	@Override
	public Message optionReply(String option, Integer msgID) {
		try {
			if(waitingForPersistentCategoryChoiceMessageId_ == msgID) {
				waitingForPersistentCategoryChoiceMessageId_ = -1;
				return new Message(sleepstartReply(option));
			}else if(this.isWaitingForAnswer_ && this.waitingForTimeReportMessageId_==msgID && !isSleeping()) {
				return new Message(this.gotUpdate(option));
			} else {
				System.err.format("wfa=%s, id: %d vs %d",this.isWaitingForAnswer_ ? "true":"false", 
						this.waitingForTimeReportMessageId_,msgID);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}
	public boolean isSleeping(){ 
		return (sleepingObj_ != null); 
	}
	public String sleepstart(JSONObject obj)
	{	
		System.err.format("TimeManager.sleepstart\n");
		if(!isWaitingForAnswer_) {
			waitingForPersistentCategoryChoiceMessageId_ = 
					rp_.sendMessageWithKeyBoard(new Message("choose the cat"), getSleepCatNames());
			return "";
		} else {
			return String.format("cannot /sleepstart because isWaitingForAnswer_=%s", 
					Boolean.toString(isWaitingForAnswer_));
		}
	}
	private JSONArray getSleepCatNames() {
		JSONArray res = new JSONArray();
		for(Object cat:categories_) {
			JSONObject obj = (JSONObject) cat;
			if( !obj.getString("canBePersistent").equals("no") ) {
				res.put(obj.getString("name"));
			}
		}
		return res;
	}
	protected String sleepstartReply(String categoryName)
	{
		sleepingObj_ = JsonUtil.FindInJSONArray(categories_, "name", categoryName);
		Document doc = new Document();
		doc.put("startsleep", new Date());
		doc.put("category", categoryName);
		sleepingTimes_.insertOne(doc);
		return String.format("sleepstart\n%s",Util.JsonObjectToTable(sleepingObj_));
	}
	public String sleepend(JSONObject obj)
	{
		
		Document lastRecord = (Document)sleepingTimes_.find().sort(Sorts.descending("startsleep")).first();
		Date now = new Date();
		sleepingTimes_.updateOne(Filters.eq("_id",lastRecord.getObjectId("_id")),
				Updates.set("endsleep", now));
		String res = String.format("you have \"%s\" for: %s", sleepingObj_.getString("name"),
				Util.milisToTimeFormat(
				now.getTime() - 
				lastRecord.getDate("startsleep").getTime())); 
		sleepingObj_ = null;
		return res;
	}
}