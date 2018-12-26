package managers;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;
import java.util.TimeZone;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import assistantbot.ResourceProvider;
import it.sauronsoftware.cron4j.Predictor;
import managers.habits.Donep;
import managers.habits.HabitManagerBase;
import managers.habits.HabitRunnable;
import managers.habits.JSONObjectCallback;
import util.JsonUtil;
import util.KeyRing;
import util.Util;
import util.parsers.FlagParser;

import static managers.habits.Constants.FAILLABELCOLOR;
import static managers.habits.Constants.HABITBOARDID;
import static managers.habits.Constants.PENDINGLISTNAME;

public class HabitManager extends HabitManagerBase
{
	enum StreakUpdateEnum{
		FAILURE,INIT,SUCCESS;
	}
	JSONArray habits_ = null;
	Hashtable<String,Date> failTimes = null;
	MongoCollection<Document> streaks_ = null;
	String pendingListId_;
	private TrelloAssistant ta_;
	private String failedListId_;
	Donep donep_;
	private String failedListId2_;

	public HabitManager(ResourceProvider rp) throws Exception
	{
		super(rp);
		
		streaks_ = rp.getMongoClient().getDatabase("logistics").getCollection("habitspunch");
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		habits_ = FetchHabits(rp.getMongoClient());
		failTimes = new Hashtable<String,Date>(habits_.length());
		pendingListId_ = ta_.findListByName(HABITBOARDID, PENDINGLISTNAME);
		failedListId_ = ta_.findListByName(HABITBOARDID, "FAILED");
		failedListId2_ = ta_.findListByName(HABITBOARDID, "FAILED2");
		
		JSONArray cards = ta_.getCardsInList(pendingListId_);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		for(Object o:cards) {
			try {
				JSONObject obj = (JSONObject)o;
				if(IsHabitPending(obj)) {
	                System.out.println(String.format("setting up reminder for %s",obj.toString()));
	                Date due = dateFormat.parse(obj.getString("due"));
					System.out.println(String.format("setting up reminder for the card %s at %s", 
							obj.getString("name"),due.toString()));
					this.setUpReminder(obj.getString("name"), due);
				}
			} catch(Exception e) {
				e.printStackTrace(System.err);
			}
		}
		donep_ = new Donep(ta_,ud_,optionMsgs_);
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
	protected JSONArray getPendingHabitNames() throws Exception {
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
		com.github.nailbiter.util.TableBuilder tb = new com.github.nailbiter.util.TableBuilder();
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
			p.setTimeZone(Util.getTimezone());
			if(!habit.optBoolean("enabled",true))
				continue;
			tb.newRow();
			tb.addToken(habit.getString("name"));
			tb.addToken(Util.DateToString(p.nextMatchingDate()));
			tb.addToken(habit.optBoolean("isWaiting") ? 
				("PEND("+ (habit.getInt("count")-habit.getInt("doneCount"))+")"):"");
			tb.addToken(habit.optBoolean("isWaiting") ?
				Util.milisToTimeFormat(failTimes.get(habit.get("name")).getTime()- (new Date().getTime())):
				Util.milisToTimeFormat(habit.getInt("delaymin")*60*1000));
			tb.addToken(this.printStreak(habit.getString("name")));
			tb.addToken(".");
		}
		return tb.toString();
	}
	public String done(String name){
		JSONArray cards = new JSONArray();
		try {
			cards = ta_.getCardsInList(pendingListId_);
		} catch (Exception e) {
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
					} catch (Exception e) {
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
	public String doneg(JSONObject res) {
		logger_.info("in doneg!");
		
		try {
			JSONArray habits = this.getPendingHabitNames();
			if(habits.length() > 1)
			{
				int id = ud_.sendMessageWithKeyBoard("which habbit?", habits);
				optionMsgs_.put(id, "done");
				return "";
			}
			else
				return this.done(habits.getString(0));
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
			logger_.info(String.format("exception: %s", e.getMessage()));
			return null;
		}
	}
	protected String getHabitsInfoShort() throws Exception {
		System.out.println("getHabitsInfoShort");
		JSONArray habits = this.getPendingHabitNames();
		System.out.println("len="+habits.length());
		com.github.nailbiter.util.TableBuilder tb = new com.github.nailbiter.util.TableBuilder();
		tb.newRow();
		tb.addToken("name_");
		for(Object o:habits) {
			String name = (String)o;
			tb.newRow();
			tb.addToken(name);
		}
		return tb.toString();
	}
	protected void updateStreaks(String name,StreakUpdateEnum code) {
		Document doc = new Document();
		doc.put("date", new Date());
		doc.put("name", name);
		if(code==StreakUpdateEnum.INIT){
			doc = null;
		}
		else if(code==StreakUpdateEnum.FAILURE){
			doc.put("status", "FAILURE");
		}
		else if(code==StreakUpdateEnum.SUCCESS){
			doc.put("status", "SUCCESS");
		}
		
		if(doc!=null)
			streaks_.insertOne(doc);
	}
	protected String printStreak(String name) {
		Document doc = (Document)streaks_.find(Filters.eq("name",name)).first();
		return String.format("%d(%d)", doc.getInteger("accum"),doc.getInteger("streak"));
	}
	@Override
	protected String getReminderMessage(String name) {
		return String.format("don't forget to execute: %s !\n%s",
				name,
				JsonUtil.FindInJSONArray(habits_, "name", name).getString("info"));
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
		JSONObject habitObj = JsonUtil.FindInJSONArray(this.habits_,"name",name);
		String onFailed = habitObj.getString("onFailed");
		updateStreaks(name, StreakUpdateEnum.FAILURE);
		try {
			ta_.setCardDuedone(id, true);
			if(onFailed.equals("putlabel")) {
				ta_.setLabel(id, FAILLABELCOLOR);
			}else if(onFailed.equals("move")) {
				ta_.moveCard(id, failedListId_);
			}else if(onFailed.equals("move2")) {
				ta_.moveCard(id, failedListId2_);
			}else if(onFailed.equals("remove")) {
				ta_.removeCard(id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void processSetReminder(String name) {
		JSONObject habitObj = JsonUtil.FindInJSONArray(habits_, "name", name); 
		int delaymin = habitObj.getInt("delaymin");
		try {
			JSONObject obj = new JSONObject()
					.put("name", name)
					.put("due", new Date(System.currentTimeMillis()+delaymin*60*1000));
			if(habitObj.has("checklist")) {
				JSONArray checklistIn = habitObj.getJSONArray("checklist");
				System.err.format("has checklist: %s\n", checklistIn.toString());
				ArrayList<String> cl = new ArrayList<String>();
				cl.add("TODO");
				for(int i = 0;i < checklistIn.length();i++) {
					cl.add(checklistIn.getString(i));
				}
				System.err.format("cl=%s\n", cl.toString());
				JSONArray checklist =new JSONArray(cl);
				System.err.format("checklist=%s\n", checklist.toString());
				obj.put("checklist", checklist);
			}
			ta_.addCard(pendingListId_, obj);
		} catch (Exception e) {
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
	@Override
	public String donep(JSONObject res) throws Exception {
		return donep_.donepFlags(res.getString("flags"));
	}
	public String donep(String code) throws JSONException, Exception {
		return donep_.donep(code);
	}
	@Override
	public String done(JSONObject res) {
		return done(res.getString("habit"));
	}
	@Override
	public String habits(JSONObject res) throws Exception {
		FlagParser fp = new FlagParser()
				.addFlag('f', "full info on habits")
				.addFlag('s', "short info on habits")
				.parse(res.getString("key"))
				;
		
		if( fp.contains('f') )
			return getHabitsInfo();
		else if( fp.contains('s') )
			return getHabitsInfoShort();
		else
			return fp.getHelp();
	}
}