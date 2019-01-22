package managers.habits;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.logging.Logger;

import org.apache.commons.collections4.Closure;
import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import it.sauronsoftware.cron4j.Scheduler;
import managers.AbstractManager;
import managers.HabitManager;
import managers.OptionReplier;
import util.AssistantBotException;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public abstract class HabitManagerBase extends AbstractManager implements OptionReplier{
	public enum HabitRunnableEnum{
		SENDREMINDER, SETFAILURE;
	}
	protected Hashtable<Integer,String> optionMsgs_ = new Hashtable<Integer,String>();
	protected ResourceProvider rp_ = null;
	protected Scheduler scheduler_ = null;
	protected Timer timer = new Timer();
	protected Logger logger_ = null;
	protected HabitManagerBase(ResourceProvider myAssistantUserData) throws Exception{
		super(GetCommands());
		logger_ = Logger.getLogger(this.getClass().getName());
		rp_ = myAssistantUserData;
		scheduler_ = myAssistantUserData.getScheduler();
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
	public static JSONArray GetCommands() {
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
	public String optionReply(String option, Integer msgID) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if(optionMsgs_.containsKey(msgID)) {
			return (String)HabitManager.class.getMethod(optionMsgs_.get(msgID),String.class)
					.invoke(this,option);
		}
		else
			return null;
	}
	
	abstract public String donep(JSONObject res) throws Exception;
	abstract public String doneg(JSONObject res) throws AssistantBotException;
	abstract public String done(JSONObject res);
	abstract public String habits(JSONObject res) throws Exception;
	abstract protected void IfWaitingForHabit(String name,Closure<JSONObject> cb);
	abstract protected String processFailure(JSONObject obj);
	abstract protected void processSetReminder(String name);
	abstract protected String getFailureMessage(String name);
	abstract protected String getReminderMessage(String name);
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
}
