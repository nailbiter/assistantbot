package managers.habits;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.TList;
import com.julienvey.trello.impl.TrelloImpl;
import com.julienvey.trello.impl.http.ApacheHttpClient;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import assistantbot.MyAssistantUserData;
import it.sauronsoftware.cron4j.Predictor;
import it.sauronsoftware.cron4j.Scheduler;
import managers.AbstractManager;
import managers.MyManager;
import managers.OptionReplier;
import util.KeyRing;
import util.LocalUtil;
import util.MyBasicBot;
import util.StorageManager;
import util.parsers.StandardParser;

public class HabitManager extends HabitManagerBase implements OptionReplier
{
	enum StreakUpdateEnum{
		FAILURE,INIT,SUCCESS;
	}
	JSONArray habits_ = null;
	Hashtable<String,Date> failTimes = null;
	private Hashtable<String,Object> hash_ = new Hashtable<String,Object>();
	MongoCollection streaks_ = null;
	private static final String HABITBOARDID = "kDCITi9O";
	private static final String PENDINGLISTNAME = "PENDING";
	TList pendingList_ = null;

	public HabitManager(Long chatID,MyBasicBot bot,Scheduler scheduler_in, MyAssistantUserData myAssistantUserData) throws Exception
	{
		super(chatID,bot,scheduler_in,myAssistantUserData);
		streaks_ = bot.getMongoClient().getDatabase("logistics").getCollection("habitstreaks");
		
		fetchHabits();
		failTimes = new Hashtable<String,Date>(habits_.length());
		fetchPendingHabits();
	}
	private void fetchPendingHabits() {
		Trello trelloApi = new TrelloImpl(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"),
				new ApacheHttpClient());
		Board board = trelloApi.getBoard(HABITBOARDID );
		System.out.println(String.format("board is named: %s(%d)", board.getName(),board.fetchLists().size()));

		List<TList> lists = board.fetchLists();
		for(TList list : lists) {
			System.out.println(String.format("list: %s", list.getName()));
			if(list.getName().equals(PENDINGLISTNAME))
				pendingList_ = list;
		}
		Date now = new Date();
		for(Card card : pendingList_.getCards()) {
			Date due = card.getDue();
//			if(due.after(now))
		}
	}
	void fetchHabits() {
		habits_ = new JSONArray();
		bot_.getMongoClient().getDatabase("logistics").getCollection("habits")
			.find(new Document("enabled",true)).forEach(new Block<Document>() {
				@Override
				public void apply(Document doc) {
					habits_.put(new JSONObject(doc.toJson()));
				}
			});
	}
	public String getHabitsInfo() throws Exception
	{
		System.out.println("getHabitsInfo");
		System.out.println("len="+habits_.length());
		util.TableBuilder tb = new util.TableBuilder();
		{
			tb.newRow();
			tb.addToken("name");
			tb.addToken("next date");
			tb.addToken("isPending?");
			tb.addToken("timeToDo");
			tb.addToken("streak");
			tb.addToken("");
		}
		for(int i = 0; i < habits_.length(); i++) {
			JSONObject habit = habits_.getJSONObject(i);
			Predictor p = new Predictor(habit.getString("cronline"));
			p.setTimeZone(LocalUtil.getTimezone());
			if(!habit.optBoolean("enabled",true))
				continue;
			tb.newRow();
			tb.addToken(habit.getString("name"));
			tb.addToken(LocalUtil.DateToString(p.nextMatchingDate()));
			tb.addToken(habit.optBoolean("isWaiting") ? 
				("PEND("+ (habit.getInt("count")-habit.getInt("doneCount"))+")"):"");
			tb.addToken(habit.optBoolean("isWaiting") ?
				LocalUtil.milisToTimeFormat(failTimes.get(habit.get("name")).getTime()- (new Date().getTime())):
				LocalUtil.milisToTimeFormat(habit.getInt("delaymin")*60*1000));
			tb.addToken(this.printStreak(habit.getString("name")));
			tb.addToken(".");
		}
		return tb.toString();
	}
	public String taskDone(String name){
		{
			final String key = "done/habit";
			if( name.isEmpty() )
				name = (String) this.hash_.get(key);
			this.hash_.put(key, name);
		}
			
		for(int i = 0; i < habits_.length(); i++)
		{
			JSONObject habit = habits_.getJSONObject(i);
			if(habit.getString("name").startsWith(name) && habit.optBoolean("isWaiting"))
			{
				habit.put("doneCount",habit.getInt("doneCount")+1);
				if(habit.getInt("doneCount")>=habit.getInt("count"))
				{
					habit.put("isWaiting", false);
					this.updateStreaks(i, StreakUpdateEnum.SUCCESS);
					return "done task "+habit.getString("name");
				}
				else
					return (habit.getInt("count")-habit.getInt("doneCount"))+" remains";
			}
		}
		return "unknown task";
	}
	protected String doneg(JSONObject res) {
		logger_.info("in doneg!");
		JSONArray habits = new JSONArray();
		
		try {
			for(int i = 0; i < habits_.length(); i++) {
				JSONObject habit = habits_.getJSONObject(i);
				if( !habit.optBoolean("enabled",true) || !habit.optBoolean("isWaiting") )
					continue;
				habits.put(habit.getString("name"));
			}
			
			if(habits.length() > 1)
			{
				int id = ud_.sendMessageWithKeyBoard("which habbit?", habits);
				optionMsgs_.add(id);
				return "hi";
			}
			else
				return this.taskDone(habits.getString(0)); 
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
			logger_.info(String.format("exception: %s", e.getMessage()));
			return null;
		}
	}
	protected String getHabitsInfoShort() {
		System.out.println("getHabitsInfoShort");
		System.out.println("len="+habits_.length());
		util.TableBuilder tb = new util.TableBuilder();
		{
			tb.newRow();
			tb.addToken("name");
			tb.addToken("isP?");
		}
		for(int i = 0; i < habits_.length(); i++) {
			JSONObject habit = habits_.getJSONObject(i);
			Predictor p = new Predictor(habit.getString("cronline"));
			if(!habit.optBoolean("enabled",true) || !habit.optBoolean("isWaiting"))
				continue;
			tb.newRow();
			tb.addToken(habit.getString("name"));
			/* NOTE: in the next line we use Date.toString() in place of
			 * LocalUtil.DateToString() which we normally use. This is so,
			 * since Scheduler is already set up for the correct timezone. 
			 */
			tb.addToken(habit.optBoolean("isWaiting") ? 
				("("+ (habit.getInt("count")-habit.getInt("doneCount"))+")"):"");
		}
		return tb.toString();
	}
	protected void updateStreaks(int index,StreakUpdateEnum code) {
		String name = habits_.getJSONObject(index).getString("name");
		if(code==StreakUpdateEnum.INIT){
			if(streaks_.count(Filters.eq("name",name))==0) {
				Document doc = new Document();
				doc.put("streak", 0);
				doc.put("accum", 0);
				streaks_.insertOne(doc);
			}
		}
		else if(code==StreakUpdateEnum.FAILURE){
			streaks_.updateOne(Filters.eq("name",name),
					Updates.combine(Updates.set("streak", 0),Updates.inc("accum", -1)));
		}
		else if(code==StreakUpdateEnum.SUCCESS){
			streaks_.updateOne(Filters.eq("name",name),
					Updates.combine(Updates.inc("streak", 1),Updates.inc("accum", 1)));
		}
	}
	protected String printStreak(String name) {
		Document doc = (Document)streaks_.find(Filters.eq("name",name)).first();
		return String.format("%d(%d)", doc.getInteger("accum"),doc.getInteger("streak"));
	}
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
	@Override
	protected String getReminderMessage(int index) {
		JSONObject habit = habits_.getJSONObject(index);
		return String.format("don't forget to execute: %s !%s",
				habit.getString("name"),
				habit.has("info") ? String.format("\n%s", habit.getString("info")) : "");
	}
	@Override
	protected String getFailureMessage(int index) {
		return String.format("you failed the task %s !", habits_.getJSONObject(index).getString("name"));
	}
	@Override
	boolean waitingForHabit(int index) {
		return habits_.getJSONObject(index).getBoolean("isWaiting");
	}
	@Override
	void processFailure(int index) {
		habits_.getJSONObject(index).put("isWaiting", false);
		updateStreaks(index, StreakUpdateEnum.FAILURE);		
	}
	@Override
	void processSetReminder(int index) {
		habits_.getJSONObject(index).put("isWaiting", true);
		failTimes.put(habits_.getJSONObject(index).getString("name"), 
				new Date(System.currentTimeMillis()+
						habits_.getJSONObject(index).getInt("delaymin")*60*1000));
		timer.schedule(new HabitRunnable(index,HabitRunnableEnum.SETFAILURE,this),
				habits_.getJSONObject(index).getInt("delaymin")*60*1000);
	}
}