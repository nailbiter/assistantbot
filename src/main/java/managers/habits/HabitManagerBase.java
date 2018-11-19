package managers.habits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.MyAssistantUserData;
import it.sauronsoftware.cron4j.Scheduler;
import managers.AbstractManager;
import managers.MyManager;
import managers.OptionReplier;
import managers.habits.HabitManagerBase.HabitRunnableEnum;
import util.MyBasicBot;
import util.parsers.StandardParser;

public abstract class HabitManagerBase implements MyManager, OptionReplier{
	public enum HabitRunnableEnum{
		SENDREMINDER, SETFAILURE;
	}
	protected Set<Integer> optionMsgs_ = new HashSet<Integer>();
	protected MyAssistantUserData ud_ = null;
	protected Scheduler scheduler_ = null;
	protected MyBasicBot bot_;
	protected Timer timer = new Timer();
	protected Logger logger_ = null;
	Long chatID_;
	protected HabitManagerBase(Long chatID,MyBasicBot bot,Scheduler scheduler_in, MyAssistantUserData myAssistantUserData){
		logger_ = Logger.getLogger(this.getClass().getName());
		ud_ = myAssistantUserData;
		bot_ = bot;
		scheduler_ = scheduler_in;
		chatID_ = chatID;
	}
	void HabitRunnableDispatch(String name,HabitRunnableEnum code)
	{
		System.out.println(String.format("HabitRunnableDispatch(%s,%s)", name,code.toString()));
		if(code == HabitRunnableEnum.SENDREMINDER) {
			bot_.sendMessage(getReminderMessage(name), chatID_);
			processSetReminder(name);
		}
		if(code==HabitRunnableEnum.SETFAILURE){
			IfWaitingForHabit(name,new JSONObjectCallback() {
				@Override
				public void run(JSONObject obj) {
					bot_.sendMessage(getFailureMessage(obj.getString("name")), chatID_);
					processFailure(obj);
				}
			});
		}
	}
	@Override
	public JSONArray getCommands() {
		JSONArray res = new JSONArray();
		
		res.put(AbstractManager.MakeCommand("habits", "list all habits and info",
				Arrays.asList(AbstractManager.MakeCommandArg("key", StandardParser.ArgTypes.string, true))));
		res.put(AbstractManager.MakeCommand("done", "done habit",
				Arrays.asList(AbstractManager.MakeCommandArg("habit", StandardParser.ArgTypes.remainder, true))));
		res.put(AbstractManager.MakeCommand("doneg", "done habit graphically",new ArrayList<JSONObject>()));
		res.put(AbstractManager.MakeCommand("donep", "done habit graphically",new ArrayList<JSONObject>()));
		
		return res;
	}

	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("habits")==0)
			{
				if(res.optString("key").contains("s"))
					return getHabitsInfoShort();
				else
					return getHabitsInfo();
			}
			if(res.getString("name").compareTo("done")==0) 
				return taskDone(res.optString("habit"));
			if(res.getString("name").compareTo("doneg")==0)
				return doneg(res);
			if(res.getString("name").equals("donep"))
				return donep(res);
		}
		return null;
	}
	public String optionReply(String option, Integer msgID) {
		if(this.optionMsgs_.contains(msgID))
			return this.taskDone(option);
		else
			return null;
	}
	
	abstract protected String donep(JSONObject res);
	abstract protected String doneg(JSONObject res);
	abstract protected String taskDone(String optString);
	abstract protected String getHabitsInfo() throws Exception;
	abstract protected String getHabitsInfoShort() throws ClientProtocolException, IOException, Exception;
	abstract protected void IfWaitingForHabit(String name,JSONObjectCallback cb);
	abstract protected void processFailure(JSONObject obj);
	abstract protected void processSetReminder(String name);
	abstract protected String getFailureMessage(String name);
	abstract protected String getReminderMessage(String name);
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
}
