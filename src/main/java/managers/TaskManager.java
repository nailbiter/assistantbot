/**
 * 
 */
package managers;

import static java.util.Arrays.asList;

import java.util.Comparator;
import java.util.Date;
import java.util.TimerTask;

import org.apache.commons.collections4.Closure;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import managers.tasks.TaskManagerBase;
import managers.tasks.TrelloMover;
import util.JsonUtil;
import util.MongoUtil;
import util.Util;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;
import static managers.habits.Constants.SEPARATOR;

/**
 * @author nailbiter
 *
 */
public class TaskManager extends TaskManagerBase implements Closure<JSONObject> {
	public TaskManager(ResourceProvider rp) throws Exception {
		super(GetCommands(),rp);
		setUpReminders();
	}
	private void setUpReminders() throws Exception {
		JSONArray cards = ta_.getCardsInList(comparators_.get(SNOOZED).middle);
		JSONArray reminders = 
				MongoUtil.GetJSONArrayFromDatabase(mc_, "logistics", POSTPONEDTASKS);
		for(Object o:reminders) {
			JSONObject obj = (JSONObject)o;
			System.err.format("set up %s\n", obj.toString(2));
			Date d = Util.MongoDateStringToLocalDate(obj.getString("date"));
			System.err.format("date: %s\n", d.toString());
			JSONObject habitObj = 
					JsonUtil.FindInJSONArray(cards, SHORTURL, obj.getString(SHORTURL));
			if( habitObj != null ) {
				setUpReminder(habitObj,d);
			} else {
				logger_.warning(
						String.format("could not setUpReminder for %s\n", obj.toString(2)));
			}
		}
	}
	public String tasks(JSONObject res) throws Exception {
		if( !res.has("tasknum") ) {
			return PrintTasks(getTasks(INBOX),this.getParamObject(mc_));
		} else if(res.getInt("tasknum")>0){
			rp_.sendMessage(PrintTask(getTasks(INBOX),res.getInt("tasknum"),ta_));
			return "";
		} else if(res.getInt("tasknum")==0) {
			return PrintTasks(getTasks(SNOOZED),this.getParamObject(mc_));
		} else if( res.getInt("tasknum") < 0 ) {
			rp_.sendMessage(PrintTask(getTasks(SNOOZED),-res.getInt("tasknum"), ta_));
			return "";
		} else {
			throw new Exception("this should not happen");
		}
	}
	public String tasknew(JSONObject obj) throws Exception {
		ImmutableTriple<Comparator<JSONObject>, String, Integer> triple = comparators_.get(INBOX);
		JSONObject res = ta_.addCard(triple.middle, new JSONObject()
				.put("name", obj.getString("name")));
		new TrelloMover(ta_,triple.middle,SEPARATOR).moveTo(res,triple.middle,triple.right);
		logToDb("tasknew",res);
		return String.format("created new card %s",res.getString("shortUrl"));
	}
	public String taskdone(JSONObject obj) throws JSONException, Exception {
		if( !obj.has("num") ) {
			return PrintDoneTasks(ta_,mc_,comparators_);
		}
		
		JSONObject card = null;
		if( obj.getInt("num") > 0 )
			card = getTasks(INBOX).get(obj.getInt("num")-1);
		else
			card = getTasks(SNOOZED).get(-obj.getInt("num")-1);
		logToDb("taskdone",card);
		ta_.archiveCard(card.getString("id"));
		return String.format("archived task \"%s\"", card.getString("name"));
	}
	public String taskpostpone(JSONObject obj) throws JSONException, Exception {
		if( !obj.has("num") ) {
			return PrintSnoozed(ta_,mc_,comparators_.get(SNOOZED).middle,getParamObject(mc_),logger_);
		}
		JSONObject card = getTasks(INBOX).get(obj.getInt("num")-1);
		
		if(obj.getString("moveToSnoozed?").toUpperCase().equals("T")) 
		{
			new TrelloMover(ta_,comparators_.get(INBOX).middle,SEPARATOR)
			.moveTo(card,comparators_.get(SNOOZED).middle,comparators_.get(SNOOZED).right);
		}
		
		Date date = ComputePostponeDate(obj.getString("estimate"));
		logToDb(String.format("%s to %s", "taskpostpone",date.toString()),card);
		System.err.format("date: %s\n", date.toString());
		setUpReminder(card,date);
		saveSnoozeToDb(card,date);
		
		return String.format("snoozing card \"%s\" to %s", 
				card.getString("name"),date.toString());
	}
	public static JSONArray GetCommands() throws Exception {
		JSONArray res = new JSONArray()
				.put(new ParseOrderedCmd("tasks","show list of tasks",
						asList(new ParseOrderedArg("tasknum",ArgTypes.integer)
								.makeOpt().j())))
				.put(new ParseOrderedCmd("taskpostpone","change task's due",
						asList(new ParseOrderedArg("num",ArgTypes.integer)
								.makeOpt().j(),
								new ParseOrderedArg("estimate",ArgTypes.string)
								.makeOpt().j(),
								new ParseOrderedArg("moveToSnoozed?",ArgTypes.string)
								.makeOpt().useDefault("t").j()
								)))
				.put(new ParseOrderedCmd("tasknew","create new task",
						asList(
								new ParseOrderedArg("name",ArgTypes.remainder).makeOpt().j()
								)))
				.put(new ParseOrderedCmd("taskdone", "mark as done", 
						asList(new ParseOrderedArg("num",ArgTypes.integer)
								.makeOpt().j())))
				
				;
		return res;
	}
	@Override
	public void execute(JSONObject card) {
		System.err.format("execute %s\n", card.toString(2));
		try {
			new TrelloMover(ta_,comparators_.get(SNOOZED).middle,SEPARATOR)
			.moveTo(card,comparators_.get(INBOX).middle,comparators_.get(INBOX).right);
			logToDb("snooze",card);
			rp_.sendMessage(String.format("snooze \"%s\"", card.getString("name")));
		} catch (Exception e) {
			e.printStackTrace();
			rp_.sendMessage(ExceptionUtils.getStackTrace(e));
		}
	}
	protected void setUpReminder(final JSONObject card, Date date) {
		Date now = new Date();
		System.err.format("now: %s\n", now.toString());
		if(date.after(now)) {
			System.err.format("scheduling %s to %s\n", card.toString(2),date.toString());
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					TaskManager.this.execute(card);
				}
			}, date.getTime()-now.getTime());
		}
	}
}
