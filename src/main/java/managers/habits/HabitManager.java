package managers.habits;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.TList;
import com.julienvey.trello.impl.TrelloImpl;
import com.julienvey.trello.impl.http.ApacheHttpClient;
import com.mongodb.Block;
import com.mongodb.MongoClient;
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
import util.Util;
import util.parsers.StandardParser;

public class HabitManager extends HabitManagerBase
{
	enum StreakUpdateEnum{
		FAILURE,INIT,SUCCESS;
	}
	JSONArray habits_ = null;
	Hashtable<String,Date> failTimes = null;
	private Hashtable<String,Object> hash_ = new Hashtable<String,Object>();
	MongoCollection<Document> streaks_ = null;
	private static final String HABITBOARDID = "kDCITi9O";
	private static final String PENDINGLISTNAME = "PENDING";
	private static final String FAILLABELCOLOR = "green";
	private TrelloImpl trelloApi_;
	String pendingList_;
	private TrelloAssistant ta_;

	public HabitManager(Long chatID,MyBasicBot bot,Scheduler scheduler_in, MyAssistantUserData myAssistantUserData) throws Exception
	{
		super(chatID,bot,scheduler_in,myAssistantUserData);
		
		streaks_ = bot.getMongoClient().getDatabase("logistics").getCollection("habitstreaks");
		trelloApi_ = new TrelloImpl(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"),
				new ApacheHttpClient());
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		habits_ = FetchHabits(bot_.getMongoClient());
		failTimes = new Hashtable<String,Date>(habits_.length());
		pendingList_ = FetchPendingList(trelloApi_).getId();
		
		JSONArray cards = ta_.getCardsInList(pendingList_);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		for(Object o:cards) {
			JSONObject obj = (JSONObject)o;
			if(IsHabitPending(obj)) {
                System.out.println(String.format("setting up reminder for %s",obj.toString()));
                Date due = dateFormat.parse(obj.getString("due"));
				System.out.println(String.format("setting up reminder for the card %s at %s", 
						obj.getString("name"),due.toString()));
				this.setUpReminder(obj.getString("name"), due);
			}
		}
	}
	private static TList FetchPendingList(TrelloImpl trelloApi) {
		Board board = trelloApi.getBoard(HABITBOARDID );
		System.out.println(String.format("board is named: %s(%d)", board.getName(),board.fetchLists().size()));

		List<TList> lists = board.fetchLists();
		for(TList list : lists) {
			System.out.println(String.format("list: %s", list.getName()));
			if(list.getName().equals(PENDINGLISTNAME))
				return list;
		}
		return null;
	}
	JSONArray FetchHabits(MongoClient mongoClient) {
		final JSONArray habits = new JSONArray();
		mongoClient.getDatabase("logistics").getCollection("habits")
			.find(Filters.eq("enabled",true)).forEach(new Block<Document>() {
				@Override
				public void apply(Document doc) {
					habits.put(new JSONObject(doc.toJson()));
					System.out.println(String.format("schedule habit %s at %s", doc.get("name"),doc.get("cronline")));
					HabitManager.this.scheduler.schedule(doc.getString("cronline"),
							new HabitRunnable(doc.getString("name"),HabitRunnableEnum.SENDREMINDER,HabitManager.this));
					HabitManager.this.updateStreaks(doc.getString("name"), StreakUpdateEnum.INIT);
				}
			});
		return habits;
	}
	protected JSONArray getPendingHabitNames() throws ClientProtocolException, IOException {
		JSONArray res = new JSONArray();
		JSONArray cards;
		cards = ta_.getCardsInList(pendingList_);
		for(Object o:cards) {
			JSONObject obj = (JSONObject)o;
			System.out.format("\tprocessing: %s\n",obj.toString());
			if(IsHabitPending(obj)) {
				res.put(obj.getString("name"));
			}
		}
		
		return res;
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
		
		JSONArray cards = new JSONArray();
		try {
			cards = ta_.getCardsInList(pendingList_);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(Object o:cards) {
			JSONObject obj = (JSONObject)o;
			if(IsHabitPending(obj)) {
				if(obj.getString("name").startsWith(name)) {
					try {
						ta_.setCardDuedone(obj.getString("id"), true);
						this.updateStreaks(obj.getString("name"), StreakUpdateEnum.SUCCESS);
						return "done task "+obj.getString("name");
					} catch (JSONException | IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return "unknown task";
	}
	static boolean IsHabitPending(JSONObject habit) {
		return !habit.optBoolean("dueComplete",false);
	}
	protected String doneg(JSONObject res) {
		logger_.info("in doneg!");
		
		try {
			JSONArray habits = this.getPendingHabitNames();
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
	protected String getHabitsInfoShort() throws ClientProtocolException, IOException {
		System.out.println("getHabitsInfoShort");
		JSONArray habits = this.getPendingHabitNames();
		System.out.println("len="+habits.length());
		util.TableBuilder tb = new util.TableBuilder();
		{
			tb.newRow();
			tb.addToken("name");
		}
		for(Object o:habits) {
			String name = (String)o;
			tb.newRow();
			tb.addToken(name);
		}
		return tb.toString();
	}
	protected void updateStreaks(String name,StreakUpdateEnum code) {
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
	protected String getReminderMessage(String name) {
		return String.format("don't forget to execute: %s !\n%s",
				name,
				Util.FindInJSONArray(habits_, "name", name).getString("info"));
	}
	@Override
	protected String getFailureMessage(String name) {
		return String.format("you failed the task %s !", name);
	}
	@Override
	void IfWaitingForHabit(String name,JSONObjectCallback cb) {
		JSONArray cards = new JSONArray();
		try {
			cards = ta_.getCardsInList(pendingList_);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(Object o:cards) {
			JSONObject obj = (JSONObject)o;
			if(obj.getString("name").equals(name)) {
				if(IsHabitPending(obj)) {
					cb.run(obj);
					return;
				}
					
			}
		}
	}
	@Override
	void processFailure(JSONObject obj) {
		updateStreaks(obj.getString("name"), StreakUpdateEnum.FAILURE);
		try {
			ta_.setCardDuedone(obj.getString("id"), true);
			ta_.setLabel(obj.getString("id"), FAILLABELCOLOR);
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	void processSetReminder(String name) {
//		Card card = new Card();
//		card.setName(name);
		int delaymin = Util.FindInJSONArray(habits_, "name", name).getInt("delaymin");
//		card.setDue();
//		pendingList_.createCard(card);
		try {
			ta_.addCard(pendingList_, new JSONObject()
					.put("name", name)
					.put("due", new Date(System.currentTimeMillis()+delaymin*60*1000)));
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		setUpReminder(name,delaymin);
	}
	void setUpReminder(String name,Date date) {
		failTimes.put(name, date);
		timer.schedule(new HabitRunnable(name,HabitRunnableEnum.SETFAILURE,this),date);
	}
	void setUpReminder(String name,int min) {
		failTimes.put(name, 
				new Date(System.currentTimeMillis()+
						min*60*1000));
		timer.schedule(new HabitRunnable(name,HabitRunnableEnum.SETFAILURE,this),
				min*60*1000);
	}
}
