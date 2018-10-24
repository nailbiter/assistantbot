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

import assistantbot.MyAssistantUserData;
import it.sauronsoftware.cron4j.Scheduler;
import util.LocalUtil;
import util.MongoUtil;
import util.MongoUtil.GetJSONArrayFromDatabase;

import static util.LocalUtil.GetJSONArrayFromDatabase;
import util.MyBasicBot;
import util.parsers.StandardParser;
import static java.util.Arrays.asList;

/**
 * @author nailbiter
 *
 */
public class TimeManager extends AbstractManager implements MyManager,Runnable, OptionReplier {
	private static final int DELAYMIN = 30;
	protected static int ROWNUM = 2;
	protected static final String NOWORKCATNAME = "useless";
	protected static final String WHEREAREYOUNOW = "北鼻，你在幹什麼？";
	private static final Date MYDEATHDATA_ = new Date(1991 + 80, 12, 24);
	protected JSONObject sleepingObj_ = null;
	Long chatID_;
	MyBasicBot bot_;
	JSONArray categories_;
	boolean isWaitingForAnswer_ = false;
	MongoCollection<Document> time_, sleepingTimes_;
	int waitingForTimeReportMessageId_ = -1;
	int waitingForPersistentCategoryChoiceMessageId_ = -1;
	
	public TimeManager(Long chatID,MyBasicBot bot,Scheduler scheduler, MongoClient mc, MyAssistantUserData maud) {
		time_ = mc.getDatabase("logistics").getCollection("time");
		chatID_ = chatID;
		bot_ = bot;
		categories_ = MongoUtil.GetJSONArrayFromDatabase(mc, "logistics", "timecats");
		scheduler.schedule(String.format("*/%d * * * *",DELAYMIN), this);
		sleepingTimes_ = mc.getDatabase("logistics").getCollection("sleepingtimes");
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
		util.TableBuilder tb = new util.TableBuilder();
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
	protected static ArrayList<List<InlineKeyboardButton>> MakeButtons(JSONArray categories)
	{
		ArrayList<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>(); 
		for(int i = 0; i < categories.length();)
		{
			buttons.add(new ArrayList<InlineKeyboardButton>());
			for(int j = 0; j < ROWNUM && i < categories.length(); j++)
			{
				JSONObject obj = categories.getJSONObject(i);
				buttons.get(buttons.size()-1).add(new InlineKeyboardButton()
						.setText(obj.getString("name"))
						.setCallbackData(obj.getString("name")));
				i++;
			}
		}
		return buttons;
	}
	private List<List<InlineKeyboardButton>> MakePerCatButtons(JSONArray categories) {
		ArrayList<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>(); 
		int i = 0;
		while(i < categories.length())
		{
			buttons.add(new ArrayList<InlineKeyboardButton>());
			int j = 0;
			while(j < ROWNUM && i < categories.length())
			{
				JSONObject obj = categories.getJSONObject(i);
				if(!obj.getString("canBePersistent").equals("no")) {
					buttons.get(buttons.size()-1).add(new InlineKeyboardButton()
							.setText(obj.getString("name"))
							.setCallbackData(obj.getString("name")));
					j++;
				}
				i++; 
			}
		}
		return buttons;
	}
	@Override
	public void run(){
		try{
			System.err.println("run this");
			
			boolean isSleeping = isSleeping();
			if(isWaitingForAnswer_) {
				writeTimeEntry(NOWORKCATNAME);
				waitingForTimeReportMessageId_ = 
						bot_.sendMessageWithKeyBoard(WHEREAREYOUNOW, chatID_, MakeButtons(categories_));
			} else if(isSleeping) {
				gotUpdate(sleepingObj_.getString("name"));
				if(sleepingObj_.getString("canBePersistent").equals("message")) {
					waitingForTimeReportMessageId_ = 
							bot_.sendMessageWithKeyBoard(WHEREAREYOUNOW, chatID_, MakeButtons(categories_));
				}
			} else {
				waitingForTimeReportMessageId_ = 
						bot_.sendMessageWithKeyBoard(WHEREAREYOUNOW, chatID_, MakeButtons(categories_));
				isWaitingForAnswer_ = true;
			}
		}
		catch(Exception e) { e.printStackTrace(System.out); }
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
		return "remaining time to live: " + LocalUtil.milisToTimeFormat(MYDEATHDATA_.getTime() - currentData.getTime());
	}
	@Override
	public JSONArray getCommands() {
		JSONArray res = new JSONArray();
		res.put(MakeCommand("timestat", "statistics about time used", 
				asList(
						MakeCommandArg("num", StandardParser.ArgTypes.integer, true),
						MakeCommandArg("key", StandardParser.ArgTypes.string, true))));
		res.put(MakeCommand("sleepstart","start sleeping", new ArrayList<JSONObject>()));
		res.put(MakeCommand("sleepend","end sleeping", new ArrayList<JSONObject>()));
		return res;
	}
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
	@Override
	public String optionReply(String option, Integer msgID) {
		try {
			if(waitingForPersistentCategoryChoiceMessageId_ == msgID) {
				waitingForPersistentCategoryChoiceMessageId_ = -1;
				return sleepstartReply(option);
			}else if(this.isWaitingForAnswer_ && this.waitingForTimeReportMessageId_==msgID && !isSleeping()) {
				return this.gotUpdate(option);
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
					bot_.sendMessageWithKeyBoard("choose the cat", chatID_, MakePerCatButtons(categories_));
			return "choose the category";
		} else {
			return String.format("cannot /sleepstart because isWaitingForAnswer_=%s", 
					Boolean.toString(isWaitingForAnswer_));
		}
	}
	protected String sleepstartReply(String categoryName)
	{
		sleepingObj_ = LocalUtil.FindInJSONArray(categories_, "name", categoryName);
		Document doc = new Document();
		doc.put("startsleep", new Date());
		doc.put("category", categoryName);
		sleepingTimes_.insertOne(doc);
		return String.format("sleepstart %s",sleepingObj_.toString());
	}
	public String sleepend(JSONObject obj)
	{
		
		Document lastRecord = (Document)sleepingTimes_.find().sort(Sorts.descending("startsleep")).first();
		Date now = new Date();
		sleepingTimes_.updateOne(Filters.eq("_id",lastRecord.getObjectId("_id")),
				Updates.set("endsleep", now));
		String res = String.format("you have \"%s\" for: %s", sleepingObj_.getString("name"),
				LocalUtil.milisToTimeFormat(
				now.getTime() - 
				lastRecord.getDate("startsleep").getTime())); 
		sleepingObj_ = null;
		return res;
	}
}
