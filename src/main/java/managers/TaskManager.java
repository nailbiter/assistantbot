/**
 * 
 */
package managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.client.MongoCollection;

import assistantbot.ResourceProvider;
import managers.tasks.TaskManagerBase;
import managers.tasks.TrelloTaskList;
import util.AssistantBotException;
import util.JsonUtil;
import util.Message;
import util.UserCollection;
import util.db.MongoUtil;
import util.parsers.ParseCommentLine;
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
		JSONArray cards = ta_.getCardsInList(comparators_.get(SNOOZED).right.get(0).getListName());
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
		HashMap<String, Object> pcl = new ParseCommentLine(ParseCommentLine.Mode.FROMLEFT)
				.parse(remainder);
		
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
		
		String rem = (String) pcl.getOrDefault(ParseCommentLine.REM, "");
		if( Pattern.matches("-?"+Digest.DIGEST_REGEX, rem) || rem.equals("0") ) {
			if(rem.equals("0")) {
				return PrintTasks(getTasks(SNOOZED, filters, rem),this.getParamObject(rp_),recognizedCatNames_);
			} else {
				rp_.sendMessage(new Message(PrintTask(getTask(rem))));
				return "";
			}
		} else {
			return PrintTasks(getTasks(INBOX, filters, rem),this.getParamObject(rp_),recognizedCatNames_);
		}
	}
	public String tasknew(JSONObject obj) throws Exception {
		ImmutablePair<Comparator<JSONObject>, List<TrelloTaskList>> pair = comparators_.get(INBOX);
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
		
		JSONObject res = pair.right.get(0).addTask(card);
		logToDb("tasknew",res);
		rp_.sendMessage(new Message(String.format("created new card %s %s"
                        ,res.getString("shortUrl")
                        ,Digest.CreateDigest(res.getString("id"))
                        )));
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
					,comparators_.get(SNOOZED).right.get(0).getListName()
					,getParamObject(rp_)
					,logger_);
		}
		String[] nums = obj.getString("num").split(","); 
		if( nums.length==1 && nums[0].equals("0") ) {
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
		for(String num:nums) cards.add(getTask(num));
		for(JSONObject card:cards) {
			if(fp_.contains('h'))
				return fp_.getHelp();
			if(parsed.containsKey(ParseCommentLine.DATE)) {
				Date due = (Date)parsed.get(ParseCommentLine.DATE);
				ta_.setCardDue(card.getString("id"), due);
				res.add(String.format("set due %s to \"%s\"", due.toString(),card.getString("name")));
			}
			if(parsed.containsKey(SNOOZEDATE)) {
				TrelloTaskList.Move(card,comparators_.get(INBOX).right.get(0)
						, comparators_.get(SNOOZED).right.get(0));
				Date date = (Date) parsed.get(SNOOZEDATE);
				logToDb(String.format("%s to %s", "taskpostpone",date.toString()),card);
				System.err.format("date: %s\n", date.toString());
				setUpReminder(card,date);
				saveSnoozeToDb(card,date);
				res.add(String.format("snoozing card \"%s\" to %s", card.getString("name"),date.toString()));
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
						logToDb(String.format("add %s", tagname),card);
						res.add(String.format("adding tag \"%s\" to \"%s\"", tagname,card.getString("name")));
					} else {
						ta_.setLabelByName(card.getString("id"), tagname, card.getString("idList")
								,TrelloAssistant.SetUnset.UNSET);
						logToDb(String.format("rm %s", tagname),card);
						res.add(String.format("removing tag \"%s\" from \"%s\"", tagname, card.getString("name")));
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
			TrelloTaskList.Move(card,comparators_.get(SNOOZED).right.get(0)
					,comparators_.get(INBOX).right.get(0)
					);
			logToDb("snooze",card);
			rp_.sendMessage(new Message(String.format("snooze \"%s\"", card.getString("name"))));
		} catch (Exception e) {
			e.printStackTrace();
			rp_.sendMessage(new Message(ExceptionUtils.getStackTrace(e)));
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
