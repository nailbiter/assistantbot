/**
 * 
 */
package managers;

import static java.util.Arrays.asList;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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
import util.ParseCommentLine;
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
			return PrintTasks(getTasks(INBOX),this.getParamObject(mc_),recognizedCats_);
		} else if(res.getInt("tasknum")>0){
			rp_.sendMessage(PrintTask(getTasks(INBOX),res.getInt("tasknum"),ta_));
			return "";
		} else if(res.getInt("tasknum")==0) {
			return PrintTasks(getTasks(SNOOZED),this.getParamObject(mc_),recognizedCats_);
		} else if( res.getInt("tasknum") < 0 ) {
			rp_.sendMessage(PrintTask(getTasks(SNOOZED),-res.getInt("tasknum"), ta_));
			return "";
		} else {
			throw new Exception("this should not happen");
		}
	}
	public String tasknew(JSONObject obj) throws Exception {
		ImmutableTriple<Comparator<JSONObject>, String, Integer> triple = comparators_.get(INBOX);
		HashMap<String, Object> parsed = 
				new ParseCommentLine(ParseCommentLine.Mode.FROMRIGHT)
				.parse(obj.getString("name"));
		JSONObject card = new JSONObject()
				.put("name", (String)parsed.get(ParseCommentLine.REM));
		if( parsed.containsKey(ParseCommentLine.DATE) )
			card.put("due", (Date)parsed.get(ParseCommentLine.DATE));
		if( parsed.containsKey(ParseCommentLine.TAGS) )
			card.put("labelByName", 
					new JSONArray((HashSet<String>)parsed.get(ParseCommentLine.TAGS)));
		
		JSONObject res = ta_.addCard(triple.middle, card);
		
		new TrelloMover(ta_,triple.middle,SEPARATOR).moveTo(res,triple.middle,triple.right);
		logToDb("tasknew",res);
		rp_.sendMessage(String.format("created new card %s",res.getString("shortUrl")));
		return "";
	}
	public String taskdone(JSONObject obj) throws JSONException, Exception {
		if( !obj.has("num") ) {
			return PrintDoneTasks(ta_,mc_,comparators_,recognizedCats_);
		}
		
		JSONObject card = getTask(obj.getInt("num"));
		logToDb("taskdone",card);
		ta_.archiveCard(card.getString("id"));
		return String.format("archived task \"%s\"", card.getString("name"));
	}
	public String taskmodify(JSONObject obj) throws JSONException, Exception {
		if( !obj.has("num") )
			return PrintSnoozed(ta_,mc_,comparators_.get(SNOOZED).middle,getParamObject(mc_),logger_);
		
		JSONObject card = getTask(obj.getInt("num"));
		
		String remainder = obj.getString("remainder");
		final String SNOOZEDATE = "SNOOZEDATE";
		HashMap<String, Object> parsed = new ParseCommentLine(ParseCommentLine.Mode.FROMLEFT)
				.addHandler(SNOOZEDATE, "%%", ParseCommentLine.TOKENTYPE.DATE)
				.parse(remainder);
		
		if(parsed.containsKey(SNOOZEDATE)) {
			new TrelloMover(ta_,comparators_.get(INBOX).middle,SEPARATOR)
			.moveTo(card,comparators_.get(SNOOZED).middle,comparators_.get(SNOOZED).right);
			Date date = (Date) parsed.get(SNOOZEDATE);
			logToDb(String.format("%s to %s", "taskpostpone",date.toString()),card);
			System.err.format("date: %s\n", date.toString());
			setUpReminder(card,date);
			saveSnoozeToDb(card,date);
			rp_.sendMessage(String.format("snoozing card \"%s\" to %s", 
					card.getString("name"),date.toString()));
		}
		if(parsed.containsKey(ParseCommentLine.TAGS)) {
			Set<String> tags = (Set<String>)parsed.get(ParseCommentLine.TAGS);
			for(String tagname:tags)
				ta_.setLabelByName(card.getString("id"), tagname, card.getString("idList"));
			rp_.sendMessage(String.format("tagging with %s", tags.toString()));
		}
		return "";
	}
	public static JSONArray GetCommands() throws Exception {
		JSONArray res = new JSONArray()
				.put(new ParseOrderedCmd("tasks","show list of tasks",
						new ParseOrderedArg("tasknum",ArgTypes.integer)
								.makeOpt()))
				.put(new ParseOrderedCmd("taskmodify","change task's due",
						new ParseOrderedArg("num",ArgTypes.integer)
								.makeOpt(),
								new ParseOrderedArg("remainder",ArgTypes.string)
								.makeOpt(),
								new ParseOrderedArg("moveToSnoozed?",ArgTypes.string)
								.makeOpt().useDefault("t")
								))
				.put(new ParseOrderedCmd("tasknew","create new task",
								new ParseOrderedArg("name",ArgTypes.remainder).makeOpt().j()
								))
				.put(new ParseOrderedCmd("taskdone", "mark as done", 
						new ParseOrderedArg("num",ArgTypes.integer)
								.makeOpt()))
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
