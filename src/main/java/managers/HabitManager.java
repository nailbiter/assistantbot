package managers;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import it.sauronsoftware.cron4j.Predictor;
import it.sauronsoftware.cron4j.Scheduler;
import util.LocalUtil;
import util.MyBasicBot;
import util.StorageManager;

public class HabitManager implements util.MyManager
{
	JSONArray habits = null;
	JSONObject streaks = null;
	Long chatID_;
	Scheduler scheduler = null;
	MyBasicBot bot_;
	Timer timer = new Timer();
	Hashtable<String,Date> failTimes = null; 
	enum HabitRunnableEnum{
		SENDREMINDER, SETFAILURE;
	}
	class HabitRunnable extends TimerTask
	{
		int index_;
		HabitRunnableEnum code_;
		HabitRunnable(int index,HabitRunnableEnum code){ index_ = index; code_ = code;}
		@Override
		public void run() { HabitRunnableDispatch(index_,code_); }
	}

	protected String getReminderMessage(int index)
	{
		return String.format("don't forget to execute %s !",
				habits.getJSONObject(index).getString("name"));
	}
	protected String getFailureMessage(int index)
	{
		return String.format("you failed the task %s !", habits.getJSONObject(index).getString("name"));
	}
	protected void HabitRunnableDispatch(int index,HabitRunnableEnum code)
	{
		System.out.println(String.format("HabitRunnableDispatch(%d,%s)", index,code.toString()));
		if(code == HabitRunnableEnum.SENDREMINDER) {
			bot_.sendMessage(getReminderMessage(index), chatID_);
			habits.getJSONObject(index).put("isWaiting", true);
			failTimes.put(habits.getJSONObject(index).getString("name"), 
					new Date(System.currentTimeMillis()+
							habits.getJSONObject(index).getInt("delaymin")*60*1000));
			timer.schedule(new HabitRunnable(index,HabitRunnableEnum.SETFAILURE),
					habits.getJSONObject(index).getInt("delaymin")*60*1000);
		}
		if(code==HabitRunnableEnum.SETFAILURE){
			if(habits.getJSONObject(index).getBoolean("isWaiting"))
			{
				habits.getJSONObject(index).put("isWaiting", false);
				//add logging
				this.updateStreaks(index, -1);
				bot_.sendMessage(getFailureMessage(index), chatID_);
			}
		}
	}
	public HabitManager(Long chatID,MyBasicBot bot,Scheduler scheduler_in) throws Exception
	{
		bot_ = bot;
		scheduler = scheduler_in;
		chatID_ = chatID;
		habits = util.StorageManager.get("habits",false).getJSONArray("obj");
		failTimes = new Hashtable<String,Date>(habits.length());
		streaks = StorageManager.get("habitstreaks",true);
		for(int i = 0; i < habits.length(); i++)
		{	
			JSONObject habit = habits.getJSONObject(i);
			if(!habit.has("count"))
				habit.put("count", 1);
			habit.put("doneCount",0);
			habit.put("isWaiting",false);
			if(habit.optBoolean("enabled",true))
			{
				scheduler.schedule(habit.getString("cronline"),
						new HabitRunnable(i,HabitRunnableEnum.SENDREMINDER));
				this.updateStreaks(i, 0);
			}
		}
	}
	public String getHabitsInfo()
	{
		System.out.println("getHabitsInfo");
		System.out.println("len="+habits.length());
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
		for(int i = 0; i < habits.length(); i++) {
			JSONObject habit = habits.getJSONObject(i);
			Predictor p = new Predictor(habit.getString("cronline"));
			if(!habit.optBoolean("enabled",true))
				continue;
			tb.newRow();
			tb.addToken(habit.getString("name"));
			tb.addToken(p.nextMatchingDate().toString());
			tb.addToken(habit.optBoolean("isWaiting") ? 
				("PEND("+ (habit.getInt("count")-habit.getInt("doneCount"))+")"):"");
			tb.addToken(habit.optBoolean("isWaiting") ?
				LocalUtil.milisToTimeFormat(failTimes.get(habit.get("name")).getTime()- (new Date().getTime())):
				LocalUtil.milisToTimeFormat(habit.getInt("delaymin")*60*1000));
			tb.addToken(this.printStreak(i));
			tb.addToken(".");
		}
		return tb.toString();
	}
	public String taskDone(String name)
	{
		for(int i = 0; i < habits.length(); i++)
		{
			JSONObject habit = habits.getJSONObject(i);
			if(habit.getString("name").startsWith(name) && habit.optBoolean("isWaiting"))
			{
				habit.put("doneCount",habit.getInt("doneCount")+1);
				if(habit.getInt("doneCount")>=habit.getInt("count"))
				{
					habit.put("isWaiting", false);
					this.updateStreaks(i, 1);
					return "done task "+habit.getString("name");
				}
				else
					return (habit.getInt("count")-habit.getInt("doneCount"))+" remains";
			}
		}
		return "unknown task";
	}
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("habits")==0)
				return getHabitsInfo();
			if(res.getString("name").compareTo("done")==0) 
				return taskDone(res.getString("habit"));
		}
		return null;
	}
	@Override
	public String gotUpdate(String data) throws Exception {
		return null;
	}
	/**
	 * @param code -1 means failure, 0 means init, 1 means success 
	 */
	protected void updateStreaks(int index,int code)
	{
		String name = habits.getJSONObject(index).getString("name");
		if(code==0)
		{
			if(streaks.optJSONObject(name)==null)
			{
				JSONObject item = new JSONObject()
						.put("streak", 0)
						.put("accum", 0);
				streaks.put(name, item);
			}
			/*if(streaks.has(habitname))
				streaks.put(habitname, 0);*/
			return;
		}
		if(code < 0)
		{
			/*
			 streaks.put(habits.getJSONObject(index).getString("name"),0);
			 */
			JSONObject item = streaks.getJSONObject(name);
			item.put("streak",0);
			item.put("accum", item.getInt("accum")-1);
			return;
		}
		if(code > 0)
		{
			/*
			 streaks.put(habit.getString("name"),
							1+streaks.optInt(habit.getString("name"), 0));
			 */
			JSONObject item = streaks.getJSONObject(name);
			item.put("streak",item.getInt("streak")+1);
			item.put("accum", item.getInt("accum")+1);
			return;
		}
	}
	protected String printStreak(int index)
	{
		JSONObject streak = streaks.getJSONObject(habits.getJSONObject(index).getString("name"));
		/*Integer.toString(this.streaks.optInt(habit.getString("name"),0));*/
		return streak.getInt("accum")+"("+streak.getInt("streak")+")";
	}
	@Override
	public JSONArray getCommands() {
		return new JSONArray("[{\"name\":\"habits\",\"args\":[],\"help\":\"list all habits and info\"}\n" + 
				",{\"name\":\"done\",\"args\":[{\"name\":\"habit\",\"type\":\"remainder\"}],\"help\":\"done habit\"}]");
	}
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
}