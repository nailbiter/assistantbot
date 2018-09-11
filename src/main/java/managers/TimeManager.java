package managers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;

import assistantbot.MyAssistantUserData;
import it.sauronsoftware.cron4j.Scheduler;
import util.LocalUtil;
import util.MyBasicBot;
import util.StorageManager;
import util.parsers.StandardParser;

/**
 * @author nailbiter
 *
 */
public class TimeManager extends AbstractManager implements MyManager,Runnable, OptionReplier {
	Scheduler scheduler_;
	protected boolean isSleeping;
	Long chatID_;
	MyBasicBot bot_;
	ArrayList<List<InlineKeyboardButton>> buttons = null;
	JSONArray categories = null;
	protected static int ROWNUM = 2;
	boolean isWaitingForAnswer;
	MyAssistantUserData userData_ = null;
	protected static final int SLEEPINDEX = 0, NOWORKINDEX = 8;
	JSONObject obj_ = null;
//	JSONArray sleepingtimes_ = null, wakingtimes_ = null;
	protected MongoClient mongoClient_;
	MongoCollection time_, sleepingTimes_, wakingTimes_;
	protected static final int DELAYMIN=30;
	
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
		util.TableBuilder tb = new util.TableBuilder();
		for(int i = 0; i < list.size(); i++){
			tb.newRow();
			tb.addToken(list.get(i).getKey()+":");
			tb.addToken(printTime(list.get(i).getValue(),res.optString("key")));
		}
		
		return tb.toString();
	}
	protected String printTime(int num, String key)
	{
		if(key.contains("n"))
			return String.format("%.1fh", num/2.0);
		else
			return StringUtils.repeat("*",num);
	}
	public TimeManager(Long chatID,MyBasicBot bot,Scheduler scheduler_in, MongoClient mongoClient, MyAssistantUserData myAssistantUserData) {
		this.mongoClient_ = mongoClient;
		time_ = mongoClient.getDatabase("logistics").getCollection("time");
		this.scheduler_ = scheduler_in;
		this.chatID_ = chatID;
		this.bot_ = bot;
		this.isWaitingForAnswer = false;
		this.userData_ = myAssistantUserData;
		makeButtons();
		scheduler_.schedule(String.format("*/%d * * * *",DELAYMIN),this);
		isSleeping = false;
		sleepingTimes_ = mongoClient.getDatabase("logistics").getCollection("sleepingtimes");
		wakingTimes_ = mongoClient.getDatabase("logistics").getCollection("wakingtimes");
	}
	protected void makeButtons()
	{
		categories = StorageManager.GetJSONArrayFromDatabase(mongoClient_, "logistics", "timecats", "name");
		buttons = new ArrayList<List<InlineKeyboardButton>>();
		for(int i = 0; i < categories.length();)
		{
			buttons.add(new ArrayList<InlineKeyboardButton>());
			for(int j = 0; j < ROWNUM && i < categories.length(); j++)
			{
				buttons.get(buttons.size()-1).add(new InlineKeyboardButton()
						.setText(categories.getString(i))
						.setCallbackData(categories.getString(i)));
				i++;
			}
		}
	}
	protected static final String WHEREAREYOUNOW = "北鼻，你在幹什麼？";
	int waitingMessageID = -1;
	@Override
	public void run(){
		try 
		{
			System.out.println("run this");
			
			if(this.isWaitingForAnswer)
			{
				if(isSleeping())
					gotUpdate(categories.getString(TimeManager.SLEEPINDEX));
				else
					gotUpdate(categories.getString(TimeManager.NOWORKINDEX));
			}
			else
			{
				if(isSleeping())
					gotUpdate(categories.getString(TimeManager.SLEEPINDEX));
				else
					waitingMessageID = bot_.sendMessageWithKeyBoard(WHEREAREYOUNOW, chatID_, buttons);
				this.isWaitingForAnswer = true;
			}
		}
		catch(Exception e) { e.printStackTrace(System.out); }
	}
	public String gotUpdate(String data) throws Exception {
		Document res = new Document();
		res.put("date", new Date());
		res.put("category", data);
		time_.insertOne(res);
		
		this.isWaitingForAnswer = false;
		return "got: "+data+"\n"+this.getLifetime();
	}
	protected String getLifetime()
	{
		Date currentData = new Date();
		Date myDeathData = new Date(1991 + 80, 12, 24);
		return "remaining time to live: " + LocalUtil.milisToTimeFormat(myDeathData.getTime() - currentData.getTime());
	}
	@Override
	public JSONArray getCommands() {
		JSONArray res = new JSONArray();
		res.put(AbstractManager.makeCommand("timestat", "statistics about time used", 
				Arrays.asList(AbstractManager.makeCommandArg("num", StandardParser.ArgTypes.integer, true),
						AbstractManager.makeCommandArg("key", StandardParser.ArgTypes.string, true)
						)));
		res.put(makeCommand("sleepstart","start sleeping",new ArrayList<JSONObject>()));
		res.put(makeCommand("sleepend","end sleeping",new ArrayList<JSONObject>()));
		return res;
	}
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
	@Override
	public String optionReply(String option, Integer msgID) {
		try {
			if(this.isWaitingForAnswer && this.waitingMessageID==msgID)
				return this.gotUpdate(option);
			else
			{
				System.out.format("wfa=%s, id: %d vs %d",this.isWaitingForAnswer ? "true":"false", 
						this.waitingMessageID,msgID);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return null;
		}
	}
	public boolean isSleeping(){ return this.isSleeping; }
	public String sleepstart(JSONObject obj)
	{
		this.isSleeping = true;
//		this.sleepingtimes_.put((new Date()).getTime());
		Document doc = new Document();
		doc.put("startsleep", new Date());
		sleepingTimes_.insertOne(doc);
		return "start sleeping";
	}
	public String sleepend(JSONObject obj)
	{
		this.isSleeping = false;
		Document lastWakeRecord = new Document();
		lastWakeRecord.put("endsleep", new Date());
		wakingTimes_.insertOne(lastWakeRecord);
		if(this.isWaitingForAnswer)
		{
			try { gotUpdate(categories.getString(TimeManager.SLEEPINDEX)); }
			catch (Exception e) {
				e.printStackTrace(System.out);
				return "cannot gotUpdate";
			}
		}
		
		final Document lastSleepRecord = (Document)sleepingTimes_.find().sort(Sorts.descending("startsleep")).first();
		return String.format("you have slept for: %s", LocalUtil.milisToTimeFormat(
				lastWakeRecord.getDate("endsleep").getTime() - 
				lastSleepRecord.getDate("startsleep").getTime()
				/*
				this.wakingtimes_.getLong(this.wakingtimes_.length() - 1) - 
				this.sleepingtimes_.getLong(this.sleepingtimes_.length() - 1)*/));
	}
}
