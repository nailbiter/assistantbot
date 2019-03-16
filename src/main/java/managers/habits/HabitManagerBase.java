package managers.habits;

import static managers.habits.Constants.FAILLABELCOLOR;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Timer;
import java.util.logging.Logger;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import assistantbot.ResourceProvider;
import it.sauronsoftware.cron4j.Predictor;
import it.sauronsoftware.cron4j.Scheduler;
import managers.AbstractManager;
import managers.HabitManager;
import managers.OptionReplier;
import util.AssistantBotException;
import util.JsonUtil;
import util.KeyRing;
import util.UserCollection;
import util.Util;

public abstract class HabitManagerBase extends AbstractManager implements OptionReplier{
	public static enum HabitRunnableEnum{
		SENDREMINDER, SETFAILURE;
	}
	protected static enum StreakUpdateEnum{
			FAILURE,INIT,SUCCESS;
		}
	protected Hashtable<Integer,String> optionMsgs_ = new Hashtable<Integer,String>();
	protected ResourceProvider rp_ = null;
	protected Scheduler scheduler_ = null;
	protected Timer timer = new Timer();
	protected Logger logger_ = null;
	protected final ArrayList<ImmutablePair<Predicate<String>, Closure<ImmutablePair<String, String>>>> FAILUREDISPATCH;
	protected TrelloAssistant ta_;
	public JSONArray habits_ = null;
	protected MongoCollection<Document> streaks_ = null;
	protected String pendingListId_;
	public Hashtable<String,Date> failTimes = null;
//	protected String failedListId_;
//	protected String failedListId2_;
	protected HabitManagerBase(ResourceProvider rp, JSONArray commands) throws Exception{
		super(commands);
		logger_ = Logger.getLogger(this.getClass().getName());
		rp_ = rp;
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		scheduler_ = rp.getScheduler();
		FAILUREDISPATCH = CreateFailureDispatch(ta_);
		streaks_ = rp.getCollection(UserCollection.HABITSPUNCH);
		habits_ = fetchHabits();
	}
	private static ArrayList<ImmutablePair<Predicate<String>, Closure<ImmutablePair<String,String>>>> CreateFailureDispatch(TrelloAssistant ta) {
		ArrayList<ImmutablePair<Predicate<String>, Closure<ImmutablePair<String,String>>>> res 
			= new ArrayList<ImmutablePair<Predicate<String>, Closure<ImmutablePair<String,String>>>>();
		res.add(new ImmutablePair<Predicate<String>, Closure<ImmutablePair<String,String>>>(
				PredicateUtils.equalPredicate("putlabel")
				,new Closure<ImmutablePair<String,String>>() {
					@Override
					public void execute(ImmutablePair<String,String> pair) {
						String id = pair.right;
						try {
							ta.setLabel(id, FAILLABELCOLOR);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}));
		res.add(new ImmutablePair<Predicate<String>, Closure<ImmutablePair<String,String>>>(
				PredicateUtils.equalPredicate("remove")
				,new Closure<ImmutablePair<String,String>>() {
					@Override
					public void execute(ImmutablePair<String,String> pair) {
						String id = pair.right;
						try {
							ta.removeCard(id);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}));
		final String PREFIX = "move:";
		res.add(new ImmutablePair<Predicate<String>,Closure<ImmutablePair<String,String>>>(new Predicate<String>() {
			@Override
			public boolean evaluate(String object) {
				return object.startsWith(PREFIX); 
			}
		},new Closure<ImmutablePair<String,String>>() {
			@Override
			public void execute(ImmutablePair<String,String> pair) {
				String listName = pair.left.substring(PREFIX.length());
				String id = pair.right;
				System.err.format("listName=%s, id=%s\n", listName,id);
				try {
					ta.moveCard(id, ta.findListByName(managers.habits.Constants.BOARDIDS.HABITS.toString(), listName));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}));
		
		return res;
	}
	protected static boolean IsHabitPending(JSONObject habit) {
		return !habit.optBoolean("dueComplete",false);
	}
	protected void habitRunnableDispatch(String name,HabitRunnableEnum code)
	{
		System.out.println(String.format("HabitRunnableDispatch(%s,%s)", name,code.toString()));
		if(code == HabitRunnableEnum.SENDREMINDER) {
			rp_.sendMessage(getReminderMessage(name));
			processSetReminder(name);
		}
		if(code==HabitRunnableEnum.SETFAILURE){
			IfWaitingForHabit(name,new Closure<JSONObject>() {
				@Override
				public void execute(JSONObject obj) {
					rp_.sendMessage( processFailure(obj) );
				}
			});
		}
	}
	public String optionReply(String option, Integer msgID) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if(optionMsgs_.containsKey(msgID)) {
			return (String)HabitManager.class.getMethod(optionMsgs_.get(msgID),String.class)
					.invoke(this,option);
		}
		else
			return null;
	}
	
	abstract public String done(JSONObject res);
	abstract public String habits(JSONObject res) throws Exception;
	abstract protected void IfWaitingForHabit(String name,Closure<JSONObject> cb);
	abstract protected void processSetReminder(String name);
	abstract protected String getFailureMessage(String name);
	abstract protected String getReminderMessage(String name);
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
	public String getHabitsInfo() throws Exception {
		System.err.println("getHabitsInfo");
		System.err.println("len="+habits_.length());
		com.github.nailbiter.util.TableBuilder tb = new com.github.nailbiter.util.TableBuilder();
		tb.addTokens("name","next date","isPending?","timeToDo","streak","");
		for(int i = 0; i < habits_.length(); i++) {
			JSONObject habit = habits_.getJSONObject(i);
			Predictor predictor = new Predictor(habit.getString("cronline"));
			String tzname = GetTimeZone(rp_);
			System.err.format("HabitManagerBase: tzname=%s\n", tzname);
			TimeZone tz = TimeZone.getTimeZone(tzname);
			predictor.setTimeZone(tz);
			if(!habit.optBoolean("enabled",true))
				continue;
			tb.newRow();
			tb.addToken(habit.getString("name"));
			Date nd = predictor.nextMatchingDate();
			System.err.format("%s: %s %s\n", this.getName(), nd, habit.getString("name"));
			System.err.format("def timezone: %s\n", TimeZone.getDefault().getID());
			tb.addToken(
					Util.DateToString(nd, tz)
					);
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
	private JSONArray fetchHabits() {
		final JSONArray habits = new JSONArray();
		rp_.getCollection(UserCollection.HABITS)
			.find(Filters.eq("enabled",true)).forEach(new Block<Document>() {
				@Override
				public void apply(Document doc) {
					JSONObject obj = new JSONObject(doc.toJson()); 
					habits.put(obj);
					System.err.println(String.format("schedule habit %s", obj.toString(2)));
					scheduler_.schedule(doc.getString("cronline"),
							new HabitRunnable(doc.getString("name")
									,HabitRunnableEnum.SENDREMINDER
									,HabitManagerBase.this));
	                System.err.format("schudeled %s successfully\n",obj.getString("name"));
					HabitManagerBase.this.updateStreaks(doc.getString("name"), StreakUpdateEnum.INIT);
				}
			});
		return habits;
	}
	protected String printStreak(String name) {
		Document doc = (Document)streaks_.find(Filters.eq("name",name)).first();
		return String.format("%d(%d)", doc.getInteger("accum"),doc.getInteger("streak"));
	}
	protected void updateStreaks(String name, StreakUpdateEnum code) {
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
	protected String processFailure(JSONObject obj) {
		String name = obj.getString("name"), id = obj.getString("id");
		JSONObject habitObj = JsonUtil.FindInJSONArray(this.habits_,"name",name);
		String onFailed = habitObj.getString("onFailed");
		updateStreaks(name, StreakUpdateEnum.FAILURE);
		try {
			ta_.setCardDuedone(id, true);
//			if(onFailed.equals("putlabel")) {
//				ta_.setLabel(id, FAILLABELCOLOR);
//			}else if(onFailed.equals("move")) {
//				ta_.moveCard(id, failedListId_);
//			}else if(onFailed.equals("move2")) {
//				ta_.moveCard(id, failedListId2_);
//			}else if(onFailed.equals("remove")) {
//				ta_.removeCard(id);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(ImmutablePair<Predicate<String>, Closure<ImmutablePair<String, String>>> d:FAILUREDISPATCH) {
			if( d.left.evaluate(onFailed) ) {
				d.right.execute(new ImmutablePair<String,String>(onFailed,id));
				break;
			}
		}
		return this.getFailureMessage( obj.getString("name") );
	}
}
