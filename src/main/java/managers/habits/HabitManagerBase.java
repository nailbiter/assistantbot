package managers.habits;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Timer;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import it.sauronsoftware.cron4j.Scheduler;
import managers.AbstractManager;
import managers.HabitManager;
import managers.OptionReplier;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

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
				Arrays.asList(ParseOrdered.MakeCommandArg("key", ParseOrdered.ArgTypes.string, true))));
		res.put(new ParseOrderedCmd("done", "done habit",
				Arrays.asList(
						(JSONObject)new ParseOrderedArg("habit", ParseOrdered.ArgTypes.remainder, true).useMemory()
						)));
		res.put(ParseOrdered.MakeCommand("doneg", "done habit graphically",new ArrayList<JSONObject>()));
		res.put(ParseOrdered.MakeCommand("donep", "done habit graphically",new ArrayList<JSONObject>()));
		return res;
	}
	public String optionReply(String option, Integer msgID) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if(optionMsgs_.containsKey(msgID)) {
			return (String)HabitManager.class.getMethod(optionMsgs_.get(msgID),String.class)
					.invoke(this,option);
		}
		else
			return null;
	}
	
	abstract public String donep(JSONObject res) throws Exception;
	abstract public String doneg(JSONObject res);
	abstract public String done(JSONObject res);
	abstract public String habits(JSONObject res) throws Exception;
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
