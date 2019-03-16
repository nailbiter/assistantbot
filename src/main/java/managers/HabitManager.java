package managers;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.Transformer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import managers.habits.Donep;
import managers.habits.HabitManagerBase;
import managers.habits.HabitRunnable;
import util.AssistantBotException;
import util.JsonUtil;
import util.Util;
import util.parsers.FlagParser;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class HabitManager extends HabitManagerBase
{
	Donep donep_;
	public HabitManager(ResourceProvider rp) throws Exception
	{
		super(rp,GetCommands());
		
		failTimes = new Hashtable<String,Date>(habits_.length());
		
		JSONArray cards = getCardsInList();
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
		donep_ = new Donep(ta_,rp_,optionMsgs_);
	}
	public String done(String name){
		JSONArray cards = new JSONArray();
		try {
			cards = getCardsInList();
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
	public String doneg(JSONObject res) throws AssistantBotException {
		logger_.info("in doneg!");
		final FlagParser fp = new FlagParser()
				.addFlag('f', "fail the task")
				.parse(res.getString("flags"));
		
		try {
			JSONArray habits = this.getPendingHabitNames();
			if(habits.length() > 1) {
				rp_.sendMessageWithKeyBoard(fp.contains('f')?"which habit to fail?":"which habit?"
						,Util.IdentityMap(habits) 
						,new Transformer<Object,String>() {
							@Override
							public String transform(Object name) {
								if(fp.contains('f')) {
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
												return processFailure(obj);
											}
										}
									}
									return String.format("no habit %s", name);
								} else {
									return done((String) name);
								}
							}
						});
				return "";
			} else {
				return done(habits.getString(0));
			}
		} catch(Exception e) {
			String s = Util.ExceptionToString(e);
			logger_.info(s);
			return s;
		}
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
	protected void IfWaitingForHabit(String name,Closure<JSONObject> cb) {
		JSONArray cards = new JSONArray();
		try {
			cards = getCardsInList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(Object o:cards) {
			JSONObject obj = (JSONObject)o;
			if(obj.getString("name").equals(name)) {
				if(IsHabitPending(obj)) {
					cb.execute(obj);
					return;
				}
			}
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
			addCard(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setUpReminder(name,delaymin);
	}
	void addCard(JSONObject obj) throws Exception {
		if( pendingListId_ != null ) {
			ta_.addCard(pendingListId_, obj);
		}
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
	public static JSONArray GetCommands() throws AssistantBotException {
		JSONArray res = new JSONArray()
			.put(new ParseOrderedCmd("habits", "list all habits and info",
				new ParseOrderedArg("key", ArgTypes.string).useDefault("s")))
			.put(new ParseOrderedCmd("done", "done habit",
				new ParseOrderedArg("habit", ArgTypes.remainder).useMemory()))
			.put(new ParseOrderedCmd("doneg", "done habit graphically",
					new ParseOrderedArg("flags",ArgTypes.string).useDefault("")))
			.put(new ParseOrderedCmd("donep", "done habit graphically"
					,new ParseOrderedArg("flags",ArgTypes.string).useDefault("c").useMemory()));
		return res;
	}
}