/**
 * 
 */
package managers;

import static managers.habits.Constants.SEPARATOR;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.client.MongoCollection;

import assistantbot.ResourceProvider;
import managers.tasks.TaskManagerBase;
import managers.tasks.TrelloMover;
import util.AssistantBotException;
import util.JsonUtil;
import util.ParseCommentLine;
import util.UserCollection;
import util.db.MongoUtil;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

/**
 * @author nailbiter
 *
 */
public class TaskManager extends TaskManagerBase implements Closure<JSONObject> {
	public TaskManager(ResourceProvider rp) throws Exception {
		super(GetCommands(),rp);
		setUpReminders(rp);
	}
	private void setUpReminders(ResourceProvider rp) throws Exception {
		JSONArray cards = ta_.getCardsInList(comparators_.get(SNOOZED).middle);
		MongoCollection<Document> coll = 
				rp_.getCollection(UserCollection.POSTPONEDTASKS);
		JSONArray reminders = MongoUtil.GetJSONArrayFromDatabase(coll);
		for(Object o:reminders) {
			JSONObject obj = (JSONObject)o;
			System.err.format("set up %s\n", obj.toString(2));
			Date d = MongoUtil.MongoDateStringToLocalDate(obj.getString("date"));
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
		String remainder = res.optString("tasknum", "");
		HashMap<String, Object> pcl = new ParseCommentLine(ParseCommentLine.Mode.FROMLEFT).parse(remainder);
		
		ArrayList<Predicate<JSONObject>> filters = new ArrayList<Predicate<JSONObject>>();
		if(pcl.containsKey(ParseCommentLine.TAGS)) {
			HashSet<String> tags = (HashSet<String>) pcl.get(ParseCommentLine.TAGS);
			for(String tag:tags) {
				if ( TASKSVIEWSPECIALTAGS_.containsKey(tag) ) {
					filters.add(TASKSVIEWSPECIALTAGS_.get(tag));
				} else {
					filters.add(new Predicate<JSONObject>() {
						@Override
						public boolean evaluate(JSONObject card) {
							return GetLabels(card.getJSONArray("labels")).contains(tag);
						}
					});
				}
			}
		}
		
		if( ((String)pcl.getOrDefault(ParseCommentLine.REM, "")).length()==0 ) {
			return PrintTasks(getTasks(INBOX),this.getParamObject(rp_),recognizedCatNames_, filters);
		}
		int tasknum = Integer.parseInt((String)pcl.getOrDefault(ParseCommentLine.REM, ""));
		if(tasknum>0){
			rp_.sendMessage(PrintTask(getTasks(INBOX),tasknum,ta_));
			return "";
		} else if(tasknum==0) {
			return PrintTasks(getTasks(SNOOZED),this.getParamObject(rp_),recognizedCatNames_, filters);
		} else if( tasknum < 0 ) {
			rp_.sendMessage(PrintTask(getTasks(SNOOZED),-tasknum, ta_));
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
	public String taskmodify(JSONObject obj) throws JSONException, Exception {
		ArrayList<AssistantBotException> exs = 
				new ArrayList<AssistantBotException>();
		HashMap<String,Integer> stat = 
				GetDoneTasksStat(ta_,rp_,comparators_,recognizedCatNames_,exs);
		
		if( !obj.has("num") ) {
			return PrintSnoozed(ta_
					,rp_
					,comparators_.get(SNOOZED).middle
					,getParamObject(rp_)
					,logger_);
		}
		ArrayList<Integer> nums = TaskManagerBase.ParseIntList(obj.getString("num"));
		if( nums.size()==1 && nums.get(0)==0 ) {
			return PrintDoneTasks(stat,exs,cats_);
		}
			
		
		String remainder = obj.getString("remainder");
		final String SNOOZEDATE = "SNOOZEDATE";
		HashMap<String, Object> parsed = new ParseCommentLine(ParseCommentLine.Mode.FROMLEFT)
				.addHandler(SNOOZEDATE, "%%", ParseCommentLine.TOKENTYPE.DATE)
				.parse(remainder);
		fp_.parse((String) parsed.getOrDefault(ParseCommentLine.REM,""));
		
		ArrayList<String> res = new ArrayList<String>();
		ArrayList<JSONObject> cards = new ArrayList<JSONObject>();
		for(Integer num:nums) cards.add(getTask(num));
		for(JSONObject card:cards) {
			if(fp_.contains('h'))
				return fp_.getHelp();
			if(parsed.containsKey(ParseCommentLine.DATE)) {
				Date due = (Date)parsed.get(ParseCommentLine.DATE);
				ta_.setCardDue(card.getString("id"), due);
				res.add(String.format("set due %s to \"%s\"", due.toString(),card.getString("name")));
			}
			if(parsed.containsKey(SNOOZEDATE)) {
				new TrelloMover(ta_,comparators_.get(INBOX).middle,SEPARATOR)
				.moveTo(card,comparators_.get(SNOOZED).middle,comparators_.get(SNOOZED).right);
				Date date = (Date) parsed.get(SNOOZEDATE);
				logToDb(String.format("%s to %s", "taskpostpone",date.toString()),card);
				System.err.format("date: %s\n", date.toString());
				setUpReminder(card,date);
				saveSnoozeToDb(card,date);
				res.add(String.format("snoozing card \"%s\" to %s", 
						card.getString("name"),date.toString()));
			}
			if( !((Set<String>)parsed.get(ParseCommentLine.TAGS)).isEmpty() ) {
				Set<String> tags = (Set<String>)parsed.get(ParseCommentLine.TAGS);
				System.err.format("card: %s\n", card.toString(2));
				JSONArray labels = 
						card.has("labels") ? card.getJSONArray("labels") : new JSONArray();
				for(String tagname:tags) {
					if(JsonUtil.FindInJSONArray(card.getJSONArray("labels"), "name", tagname) == null) {
						ta_.setLabelByName(card.getString("id"), tagname, card.getString("idList")
								,TrelloAssistant.SetUnset.SET);
						res.add(String.format("adding tag \"%s\"", tagname));
					} else {
						ta_.setLabelByName(card.getString("id"), tagname, card.getString("idList")
								,TrelloAssistant.SetUnset.UNSET);
						res.add(String.format("removing tag \"%s\"", tagname));
					}
				}
			}
			
			if( fp_.contains('h') ) {
				return fp_.getHelp();
			}
			if(fp_.contains('d')) {
				TaskManagerBase.CannotDoTask(cats_
						,GetMainLabel(GetLabels(card.getJSONArray("labels"))
								, recognizedCatNames_)
						,stat);
				logToDb("taskdone",card);
				res.add(String.format("%s task \"%s\""
						, "done",card.getString("name")));
			}
			if( fp_.contains('a') ) {
				ta_.archiveCard( card.getString("id") );
				res.add(String.format("%s task \"%s\""
						, "archived",card.getString("name")));
			}
		}
		return String.join("\n", res);
	}
	public static JSONArray GetCommands() throws Exception {
		JSONArray res = new JSONArray()
				.put(new ParseOrderedCmd("tasks","show list of tasks",
						new ParseOrderedArg("tasknum",ArgTypes.remainder)
								.makeOpt()))
				.put(new ParseOrderedCmd("taskmodify","change task's due",
						new ParseOrderedArg("num",ArgTypes.string)
								.makeOpt()
								,new ParseOrderedArg("remainder",ArgTypes.remainder)
								.makeOpt()
								))
				.put(new ParseOrderedCmd("tasknew","create new task",
								new ParseOrderedArg("name",ArgTypes.remainder).makeOpt().j()
								))
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
