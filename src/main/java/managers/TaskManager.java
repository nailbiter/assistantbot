/**
 * 
 */
package managers;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.MongoClient;

import assistantbot.ResourceProvider;
import managers.tasks.Task;
import managers.tasks.TaskManagerForTask;
import util.KeyRing;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

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
	private MongoClient mc_;
	protected static int REMINDBEFOREMIN = 10;
	protected static String TASKNAMELENLIMIT = "TASKNAMELENLIMIT";
	public TaskManager(ResourceProvider rp) throws Exception {
		super(GetCommands());
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		rp_ = rp;
		listid_ = 
				ta_.findListByName(managers.habits.Constants.INBOXBOARDID, 
						managers.habits.Constants.INBOXLISTNAME);
		mc_ = rp.getMongoClient();
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
		int TNL = this.getParamObject(mc_).getInt(TASKNAMELENLIMIT);
		if(res.has("tasknum")) {
//			return arr.getJSONObject(sepIndex+res.getInt("tasknum")).toString(2);
			return arr.getJSONObject(sepIndex+res.getInt("tasknum")).getString("url");
		} else {
			TableBuilder tb = new TableBuilder();
			tb.newRow();
			tb.addToken("#_");
			tb.addToken("name_");
			tb.addToken("labels_");
			for(int i = sepIndex+1;i < arr.length(); i++) {
				JSONObject card = arr.getJSONObject(i);
				tb.newRow();
				tb.addToken(i - sepIndex);
				tb.addToken(card.getString("name"),TNL);
				tb.addToken(GetLabel(card),TNL);
			}
			return tb.toString();
		}
	}
	private String GetLabel(JSONObject card) {
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

	String getTasks() throws Exception
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
						Arrays.asList(new ParseOrderedArg("taskid",ArgTypes.integer).j())))
				.put(new ParseOrderedCmd("taskpostpone","postpone a task",
						asList(new ParseOrderedArg("estimate",ArgTypes.integer).j())))
				.put(new ParseOrderedCmd("tasks","show list of tasks",
						asList(new ParseOrderedArg("tasknum",ArgTypes.integer).makeOpt().j())))
				.put(new ParseOrderedCmd("tasknew","create new task",
						asList(new ParseOrderedArg("estimate",ArgTypes.integer).j(),
								new ParseOrderedArg("description",ArgTypes.remainder).makeOpt().j()
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
