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

class HabitManager {
	JSONArray habits = null;
	Long chatID_;
	Scheduler scheduler = new Scheduler();
	MyAssistantBot bot_;
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
	protected void sendMessage(String msg)
	{
		try 
		{
			SendMessage message = new SendMessage()
					.setChatId(chatID_)
							.setText(msg);
			bot_.sendMessage(message);
		}
		catch(Exception e){ e.printStackTrace(System.out); }
	}
	protected String getReminderMessage(int index)
	{
		return "don't forget to execute "
				+habits.getJSONObject(index).getString("name")
				+" !";
	}
	protected String getFailureMessage(int index)
	{
		return "you failed the task "
				+habits.getJSONObject(index).getString("name")
				+" !";
	}
	protected void HabitRunnableDispatch(int index,HabitRunnableEnum code)
	{
		if(code == HabitRunnableEnum.SENDREMINDER) {
			sendMessage(getReminderMessage(index));
			habits.getJSONObject(index).put("isWaiting", true);
			failTimes.put(habits.getJSONObject(index).getString("name"), 
					new Date(System.currentTimeMillis()+
							habits.getJSONObject(index).getInt("delaymin")*60*1000));
			timer.schedule(new HabitRunnable(index,HabitRunnableEnum.SETFAILURE),
					habits.getJSONObject(index).getInt("delaymin")*60*1000);
		}
		if(code==HabitRunnableEnum.SETFAILURE){
			//FIXME: add logging
			if(habits.getJSONObject(index).getBoolean("isWaiting"))
			{
				habits.getJSONObject(index).put("isWaiting", false);
				sendMessage(getFailureMessage(index));
			}
		}
	}
	public HabitManager(Long chatID,MyAssistantBot bot)
	{
		try
		{
			bot_ = bot;
			chatID_ = chatID;
			habits = util.LocalUtil.getJSONArrayFromRes(this, "habits");
			failTimes = new Hashtable<String,Date>(habits.length());
			for(int i = 0; i < habits.length(); i++)
			{
				JSONObject habit = habits.getJSONObject(i);
				if(!habit.has("count"))
					habit.put("count", 1);
				habit.put("doneCount",0);
				scheduler.schedule(habit.getString("cronline"),new HabitRunnable(i,
						HabitRunnableEnum.SENDREMINDER));
			}
			scheduler.start();
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
	public String getHabitsInfo()
	{
		StringBuilder res = new StringBuilder();
		Formatter formatter = new Formatter();
		for(int i = 0; i < habits.length(); i++) {
			JSONObject habit = habits.getJSONObject(i);
			Predictor p = new Predictor(habit.getString("cronline"));
			res.append(formatter.format("%-20s%-40s%-20s%-20s", 
					habit.getString("name"),p.nextMatchingDate().toString(),
					habit.optBoolean("isWaiting") ? ("PEND("+
					(habit.getInt("count")-habit.getInt("doneCount"))
					+")"):"",
					habit.optBoolean("isWaiting") ?
							milisToTimeFormat(failTimes.get(habit.get("name")).getTime()
									- (new Date().getTime())) : ""
							));
		}
		return res.toString();
	}
	protected static String milisToTimeFormat(long millis)
	{
		return Integer.toString((int)(millis/1000.0/60.0/60.0)) + "h:"+
				Integer.toString((int)((millis/1000.0/60.0)%60)) + "m:"+
				Integer.toString((int)((millis/1000.0)%60)) + "s:";
	}
	public String taskDone(String name)
	{
		for(int i = 0; i < habits.length(); i++) {
			JSONObject habit = habits.getJSONObject(i);
			if(habit.getString("name").startsWith(name))
			{
				habit.put("doneCount",habit.getInt("doneCount")+1);
				if(habit.getInt("doneCount")>=habit.getInt("count"))
				{
					habit.put("isWaiting", false);
					return "done task "+habit.getString("name");
				}
				else
					return (habit.getInt("count")-habit.getInt("doneCount"))+" remains";
			}
		}
		return "unknown task";
	}
}