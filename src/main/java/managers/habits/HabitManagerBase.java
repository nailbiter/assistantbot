package managers.habits;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Timer;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import it.sauronsoftware.cron4j.Scheduler;
import managers.AbstractManager;
import managers.HabitManager;
import managers.MyManager;
import managers.OptionReplier;
import util.parsers.ParseOrdered;
import util.parsers.StandardParserInterpreter;

public abstract class HabitManagerBase extends AbstractManager implements OptionReplier{
	public enum HabitRunnableEnum{
		SENDREMINDER, SETFAILURE;
	}
	protected Hashtable<Integer,String> optionMsgs_ = new Hashtable<Integer,String>();
	protected ResourceProvider ud_ = null;
	protected Scheduler scheduler_ = null;
	protected Timer timer = new Timer();
	protected Logger logger_ = null;
	protected HabitManagerBase(ResourceProvider myAssistantUserData){
		super(GetCommands());
		logger_ = Logger.getLogger(this.getClass().getName());
		ud_ = myAssistantUserData;
		scheduler_ = myAssistantUserData.getScheduler();
	}
	void HabitRunnableDispatch(String name,HabitRunnableEnum code)
	{
		System.out.println(String.format("HabitRunnableDispatch(%s,%s)", name,code.toString()));
		if(code == HabitRunnableEnum.SENDREMINDER) {
			ud_.sendMessage(getReminderMessage(name));
			processSetReminder(name);
		}
		if(code==HabitRunnableEnum.SETFAILURE){
			IfWaitingForHabit(name,new JSONObjectCallback() {
				@Override
				public void run(JSONObject obj) {
					ud_.sendMessage(getFailureMessage(obj.getString("name")));
					processFailure(obj);
				}
			});
		}
	}
	public static JSONArray GetCommands() {
		JSONArray res = new JSONArray();
		
		res.put(ParseOrdered.MakeCommand("habits", "list all habits and info",
				Arrays.asList(ParseOrdered.MakeCommandArg("key", StandardParserInterpreter.ArgTypes.string, true))));
		res.put(ParseOrdered.MakeCommand("done", "done habit",
				Arrays.asList(ParseOrdered.MakeCommandArg("habit", StandardParserInterpreter.ArgTypes.remainder, true))));
		res.put(ParseOrdered.MakeCommand("doneg", "done habit graphically",new ArrayList<JSONObject>()));
		res.put(ParseOrdered.MakeCommand("donep", "done habit graphically",new ArrayList<JSONObject>()));
		
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
	public String optionReply(String option, Integer msgID) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if(optionMsgs_.containsKey(msgID)) {
			return (String)HabitManager.class.getMethod(optionMsgs_.get(msgID),String.class)
					.invoke(this,option);
		}
		else
			return null;
	}
	
	abstract protected String donep(JSONObject res) throws Exception;
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
