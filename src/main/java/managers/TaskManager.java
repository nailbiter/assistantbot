/**
 * 
 */
package managers;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.Closure;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.MongoClient;

import assistantbot.ResourceProvider;
import managers.tasks.Task;
import managers.tasks.TaskManagerForTask;
import managers.tasks.TrelloMover;
import util.KeyRing;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;
import static managers.habits.Constants.SEPARATOR;

/**
 * @author nailbiter
 *
 */
public class TaskManager extends AbstractManager implements TaskManagerForTask,Closure<JSONObject> {
	Timer timer = new Timer();
	protected List<Task> tasks = null;
	protected JSONArray jsontasks = null;
	private ResourceProvider rp_;
	private TrelloAssistant ta_;
	private MongoClient mc_;
	protected static int REMINDBEFOREMIN = 10;
	protected static String TASKNAMELENLIMIT = "TASKNAMELENLIMIT";
	protected static String INBOX = "INBOX";
	protected static String SNOOZED = "SNOOZED";
	protected HashMap<String,ImmutableTriple<Comparator<JSONObject>,String,Integer>> comparators_
		= new HashMap<String,ImmutableTriple<Comparator<JSONObject>,String,Integer>>();
	private Timer timer_;
	public TaskManager(ResourceProvider rp) throws Exception {
		super(GetCommands());
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		rp_ = rp;
		
		mc_ = rp.getMongoClient();
		timer_ = new Timer();
		FillTable(comparators_,ta_);
	}
	private static void FillTable(HashMap<String, ImmutableTriple<Comparator<JSONObject>, String, Integer>> c, TrelloAssistant ta) throws Exception {
		String listid = ta.findListByName(managers.habits.Constants.INBOXBOARDID, 
				managers.habits.Constants.INBOXLISTNAME);
		c.put(INBOX, new ImmutableTriple<Comparator<JSONObject>,String,Integer>(
				new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						return o1.getString("name")
								.compareTo(o2.getString("name"));
					}
				},
				listid, 1));
		c.put(SNOOZED, new ImmutableTriple<Comparator<JSONObject>,String,Integer>(
				new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						return o1.getString("name")
								.compareTo(o2.getString("name"));
					}
				},
				listid, 0));
		
	}
	protected ArrayList<JSONObject> getTasks(String identifier) throws Exception{
		if( !comparators_.containsKey(identifier) )
			throw new Exception(String.format("unknown key %s", identifier));
		ImmutableTriple<Comparator<JSONObject>, String, Integer> triple = 
				comparators_.get(identifier);
		TrelloMover tm = new TrelloMover(ta_,triple.middle,SEPARATOR); 
		ArrayList<JSONObject> res = tm.getCardsInSegment(triple.right);
		Collections.sort(res, triple.left);
		return res;
	}
	protected static String PrintTasks(ArrayList<JSONObject> arr,int TNL) {
		TableBuilder tb = new TableBuilder();
		tb.newRow();
		tb.addToken("#_");
		tb.addToken("name_");
		tb.addToken("labels_");
//		tb.addToken("pos_");
		for(int i = 0;i < arr.size(); i++) {
			JSONObject card = arr.get(i);
			tb.newRow();
			tb.addToken(i + 1);
			tb.addToken(card.getString("name"),TNL);
			tb.addToken(GetLabel(card),TNL);
//			tb.addToken(card.getInt("pos"));
		}
		return tb.toString();
	}
	protected static String PrintTask(ArrayList<JSONObject> arr,int index) {
		return String.format("%s %s",
				arr.get(index-1).getString("name"),
				arr.get(index-1).getString("shortUrl"));
	}
	public String tasks(JSONObject res) throws Exception {
		int TNL = this.getParamObject(mc_).getInt(TASKNAMELENLIMIT);
		if( !res.has("tasknum") ) {
			return PrintTasks(getTasks(INBOX),TNL);
		} else if(res.getInt("tasknum")>0){
			rp_.sendMessage(PrintTask(getTasks(INBOX),res.getInt("tasknum")));
			return "";
		} else if(res.getInt("tasknum")==0) {
			return PrintTasks(getTasks(SNOOZED),TNL);
		} else if( res.getInt("tasknum") < 0 ) {
			rp_.sendMessage(PrintTask(getTasks(SNOOZED),-res.getInt("tasknum")));
			return "";
		} else {
			throw new Exception("this should not happen");
		}
	}
	private static String GetLabel(JSONObject card) {
		JSONArray label = card.optJSONArray("labels");
		if(label==null)
			return "";
		StringBuilder sb = new StringBuilder();
		for(Object o:label) {
			JSONObject obj = (JSONObject)o;
			if(obj.has("name"))
				sb.append(String.format("#%s, ", obj.getString("name")));
		}
		return sb.toString();
	}
	public String tasknew(JSONObject res)
	{
		Task task = new Task(jsontasks.length(),this);
		
		if( res.getInt("estimate") > 0 )
			task.setInitialEstimate(res.getInt("estimate"));
		task.setDescription(res.optString("description", "<no descr>"));
			
		tasks.add(task);
		jsontasks.put(task.getJSONObject());
		return String.format("schedule task #%d to be exec within %s min", 
				tasks.size()-1,
				( res.getInt("estimate") > 0 ) ? Integer.toString(res.getInt("estimate")) : "∞");
	}
	private String getTasks(int tasknum) throws Exception{
		com.github.nailbiter.util.TableBuilder tb = new com.github.nailbiter.util.TableBuilder();
		tb.addNewlineAndTokens(new String[] {"#", "completion date","description"});
		int index = tasks.size() - 1;
		while(tasknum>0 && index >=0)
		{
			if(tasks.get(index).isDone())
			{
				tb.newRow();
				tb.addToken(index);
				tb.addToken(tasks.get(index).getCompletionDate());
				tb.addToken(tasks.get(index).getDescription());
				tasknum--;
			}
			index--;
		}
		return tb.toString();
	}
	private String getTasks() throws Exception
	{
		com.github.nailbiter.util.TableBuilder tb = new com.github.nailbiter.util.TableBuilder();
		tb.addNewlineAndTokens(new String[] {"#","duedate", "remaining time","description"});
		
		for(int i = 0; i < tasks.size(); i++)
			if(!tasks.get(i).isDone())
			{
				tb.newRow();
				tb.addToken(i);
				tb.addToken(tasks.get(i).getDuedate());
				tb.addToken(tasks.get(i).getRemainingTime());
				tb.addToken(tasks.get(i).getDescription());
			}
		return tb.toString();
	}
	public String taskpostpone(JSONObject obj) throws JSONException, Exception {
		JSONObject card = getTasks(INBOX).get(obj.getInt("num")-1);
//		if(obj.getString("moveToSnoozed?").length()>0) 
		{
			new TrelloMover(ta_,comparators_.get(INBOX).middle,SEPARATOR)
			.moveTo(card,comparators_.get(SNOOZED).middle,comparators_.get(SNOOZED).right);
		}
		
		Date date = ComputePostponeDate(obj.getString("estimate"));
		System.err.format("date: %s\n", date.toString());
		setUpSnooze(card,date);
		saveSnoozeToDb(card,date);
		
		return String.format("snoozing card \"%s\" to %s", 
				card.getString("name"),date.toString());
	}
	private void saveSnoozeToDb(JSONObject card, Date date) {
		mc_.getDatabase("logistics").getCollection("postponedTasks")
		.insertOne(Document.parse(new JSONObject()
				.put("date", date)
				.put("shortUrl", card.getString("shortUrl"))
				.toString()));
	}
	private void setUpSnooze(final JSONObject card, Date date) {
		Date now = new Date();
		System.err.format("now: %s\n", now.toString());
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				TaskManager.this.execute(card);
			}
		}, date.getTime()-now.getTime());
	}
	private Date ComputePostponeDate(String string) throws Exception {
		Matcher m = null;
		Calendar c = Calendar.getInstance();
		if((m = Pattern.compile("(\\d{2})(\\d{2})(\\d{2})(\\d{2})").matcher(string)).matches()) {
//			SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmm");
//			dateFormat.setTimeZone(TimeZone.getTimeZone("JST"));
//			return dateFormat.parse(string);
			c.set(Calendar.MONTH, Integer.parseInt(m.group(1)));
			c.set(Calendar.DATE, Integer.parseInt(m.group(2)));
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(3)));
			c.set(Calendar.MINUTE, Integer.parseInt(m.group(4)));
			return c.getTime();
		} if((m = Pattern.compile("(\\d{2})(\\d{2})").matcher(string)).matches()) {
//			SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
//			dateFormat.setTimeZone(TimeZone.getTimeZone("JST"));
//			return dateFormat.parse(string);
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(1)));
			c.set(Calendar.MINUTE, Integer.parseInt(m.group(2)));
			return c.getTime();
		} else {
			throw new Exception(String.format("cannot parse %s", string));
		}
	}
	public static JSONArray GetCommands() throws Exception {
		JSONArray res = new JSONArray()
				.put(new ParseOrderedCmd("tasks","show list of tasks",
						asList(new ParseOrderedArg("tasknum",ArgTypes.integer)
								.makeOpt().j())))
				.put(new ParseOrderedCmd("taskpostpone","change task's due",
						asList(new ParseOrderedArg("num",ArgTypes.integer).j(),
								new ParseOrderedArg("estimate",ArgTypes.string)
								.j()
//								,new ParseOrderedArg("moveToSnoozed?",ArgTypes.string)
//								.makeOpt().useDefault("").j()
								)))
				//TODO
				.put(new ParseOrderedCmd("taskdone", "mark as done", 
						asList(new ParseOrderedArg("taskid",ArgTypes.integer)
								.j())))
				//TODO
				.put(new ParseOrderedCmd("tasknew","create new task",
						asList(new ParseOrderedArg("estimate",ArgTypes.integer)
								.j(),
								new ParseOrderedArg("description",ArgTypes.remainder)
								.makeOpt().j())));
		return res;
	}

	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
	@Override
	public void sendMessage(String msg) {
		rp_.sendMessage(msg);
	}
	@Override
	public void schedule(TimerTask tt, Date d) {
		timer.schedule(tt, d);
	}
	@Override
	public void execute(JSONObject card) {
		System.err.format("execute %s\n", card.toString(2));
		try {
			new TrelloMover(ta_,comparators_.get(SNOOZED).middle,SEPARATOR)
			.moveTo(card,comparators_.get(INBOX).middle,comparators_.get(INBOX).right);
			rp_.sendMessage(String.format("snooze \"%s\"", card.getString("name")));
		} catch (Exception e) {
			e.printStackTrace();
			rp_.sendMessage(ExceptionUtils.getStackTrace(e));
		}
	}

}
