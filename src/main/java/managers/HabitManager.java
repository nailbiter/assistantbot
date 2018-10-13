package managers;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import org.apache.http.client.ClientProtocolException;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import managers.habits.HabitManagerBase;
import managers.habits.HabitRunnable;
import managers.habits.JSONObjectCallback;
import util.KeyRing;
import util.LocalUtil;
import util.MyBasicBot;
import util.TrelloAssistant;
import static util.Util.FindInJSONArray;

public class HabitManager extends HabitManagerBase
{
	enum StreakUpdateEnum{
		FAILURE,INIT,SUCCESS;
	}
	JSONArray habits_ = null;
	Hashtable<String,Date> failTimes = null;
	private Hashtable<String,Object> hash_ = new Hashtable<String,Object>();
	MongoCollection<Document> streaks_ = null;
	public static final String HABITBOARDID = "kDCITi9O";
	private static final String PENDINGLISTNAME = "PENDING";
	private static final String FAILLABELCOLOR = "green";
	private TrelloImpl trelloApi_;
	String pendingListId_;
	private TrelloAssistant ta_;
	private String failedListId_;

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
		pendingListId_ = FetchPendingListId(trelloApi_);
		failedListId_ = ta_.findListByName(HABITBOARDID, "FAILED");
		
		JSONArray cards = ta_.getCardsInList(pendingListId_);
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
	private String FetchPendingListId(TrelloImpl trelloApi) throws Exception {
		return ta_.findListByName(HABITBOARDID, PENDINGLISTNAME);
	}
	JSONArray FetchHabits(MongoClient mongoClient) {
		final JSONArray habits = new JSONArray();
		mongoClient.getDatabase("logistics").getCollection("habits")
			.find(Filters.eq("enabled",true)).forEach(new Block<Document>() {
				@Override
				public void apply(Document doc) {
					JSONObject obj = new JSONObject(doc.toJson()); 
					habits.put(obj);
					System.out.println(String.format("schedule habit %s", obj.toString(2)));
					HabitManager.this.scheduler_.schedule(doc.getString("cronline"),
							new HabitRunnable(doc.getString("name"),HabitRunnableEnum.SENDREMINDER,HabitManager.this));
                    System.err.format("schudeled %s successfully\n",obj.getString("name"));
					HabitManager.this.updateStreaks(doc.getString("name"), StreakUpdateEnum.INIT);
				}
			});
		return habits;
	}
	protected JSONArray getPendingHabitNames() throws ClientProtocolException, IOException {
		JSONArray res = new JSONArray();
		JSONArray cards;
		cards = ta_.getCardsInList(pendingListId_);
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
			cards = ta_.getCardsInList(pendingListId_);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(Object o:cards) {
			JSONObject obj = (JSONObject)o;
			if(IsHabitPending(obj)) {
				if(obj.getString("name").startsWith(name)) {
					try {
						ta_.removeCard(obj.getString("id"));
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
				FindInJSONArray(habits_, "name", name).getString("info"));
	}
	@Override
	protected String getFailureMessage(String name) {
		return String.format("you failed the task %s !", name);
	}
	@Override
	protected void IfWaitingForHabit(String name,JSONObjectCallback cb) {
		JSONArray cards = new JSONArray();
		try {
			cards = ta_.getCardsInList(pendingListId_);
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
	protected void processFailure(JSONObject obj) {
		String name = obj.getString("name"), id = obj.getString("id");
		JSONObject habitObj = FindInJSONArray(this.habits_,"name",name);
		String onFailed = habitObj.getString("onFailed");
		updateStreaks(name, StreakUpdateEnum.FAILURE);
		try {
			ta_.setCardDuedone(id, true);
			if(onFailed.equals("putlabel")) {
				ta_.setLabel(id, FAILLABELCOLOR);
			}else if(onFailed.equals("move")) {
				ta_.moveCard(id, failedListId_);
			}else if(onFailed.equals("remove")) {
				ta_.removeCard(id);
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void processSetReminder(String name) {
		int delaymin = FindInJSONArray(habits_, "name", name).getInt("delaymin");
		try {
			ta_.addCard(pendingListId_, new JSONObject()
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
				(long)min*60*1000);
	}
}
