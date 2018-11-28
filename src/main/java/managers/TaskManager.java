/**
 * 
 */
package managers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;

import assistantbot.ResourceProvider;
import managers.tasks.Task;
import managers.tasks.TaskManagerForTask;
import util.KeyRing;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

import static util.parsers.ParseOrdered.ArgTypes;
import static java.util.Arrays.asList;

/**
 * @author nailbiter
 *
 */
public class TaskManager extends AbstractManager implements TaskManagerForTask {
	Timer timer = new Timer();
	protected List<Task> tasks = null;
	protected JSONArray jsontasks = null;
	private ResourceProvider rp_;
	private TrelloAssistant ta_;
	private String listid_;
	protected static int REMINDBEFOREMIN = 10;
	public TaskManager(ResourceProvider rp) throws Exception {
		super(GetCommands());
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		rp_ = rp;
		listid_ = 
				ta_.findListByName(managers.habits.Constants.INBOXBOARDID, managers.habits.Constants.INBOXLISTNAME);
	}
	protected int getSeparatorIndex(JSONArray cards) throws Exception{
		int res = IterableUtils.indexOf(cards, new Predicate<Object>() {
			@Override
			public boolean evaluate(Object arg0) {
				System.err.format("check: %s\n", arg0.toString());
				JSONObject obj = (JSONObject)arg0;
				return ((JSONObject)obj).getString("name").equals(managers.habits.Constants.SEPARATOR);
			}
		});
		if(res<0)
			throw new Exception("no separator");
		return res;
	}
	public String tasks(JSONObject res) throws Exception {
		JSONArray arr = ta_.getCardsInList(listid_);
		int sepIndex = getSeparatorIndex(arr);
		System.err.format("sepIndex = %d\n", sepIndex);
		TableBuilder tb = new TableBuilder();
		tb.newRow();
		tb.addToken("name_");
		for(int i = sepIndex+1;i < arr.length(); i++) {
			tb.newRow();
			tb.addToken(arr.getJSONObject(i).getString("name"));
		}
		return tb.toString();
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

	String getTasks () throws Exception
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

	public static JSONArray GetCommands() {
		JSONArray res = new JSONArray()
				.put(new ParseOrderedCmd("taskdone", "mark as done", 
						Arrays.asList((JSONObject)new ParseOrderedArg("taskid",ArgTypes.integer))))
				.put(new ParseOrderedCmd("taskpostpone","postpone a task",
						asList((JSONObject)new ParseOrderedArg("estimate",ArgTypes.integer))))
				.put(new ParseOrderedCmd("tasks","show list of tasks",
						asList((JSONObject) new ParseOrderedArg("tasknum",ArgTypes.integer).makeOpt())))
				.put(new ParseOrderedCmd("tasknew","create new task",
						asList((JSONObject) new ParseOrderedArg("estimate",ArgTypes.integer),
								(JSONObject) new ParseOrderedArg("description",ArgTypes.remainder).makeOpt()
								)));
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

}
