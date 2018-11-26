/**
 * 
 */
package managers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.MyAssistantUserData;
import assistantbot.ResourceProvider;
import managers.tasks.Task;
import managers.tasks.TaskManagerForTask;
import util.Util;
import util.MyBasicBot;
import util.StorageManager;
import util.parsers.ParseOrdered;
import static util.parsers.ParseOrdered.ArgTypes;
import static util.parsers.ParseOrdered.MakeCommand;
import static util.parsers.ParseOrdered.MakeCommandArg;

/**
 * @author nailbiter
 *
 */
public class TaskManager extends AbstractManager implements TaskManagerForTask {
//	protected Long chatID_ = null;
//	protected MyBasicBot bot_ = null;
	Timer timer = new Timer();
	protected List<Task> tasks = null;
	protected JSONArray jsontasks = null;
	private ResourceProvider rp_;
	protected static int REMINDBEFOREMIN = 10;
//	public TaskManager(Long chatID, MyBasicBot bot)
//	{
////		bot_ = bot;
//		JSONObject obj = StorageManager.get("tasks", true);
//		if(!obj.has("tasks"))
//			obj.put("tasks", new JSONArray());
//		jsontasks = obj.getJSONArray("tasks");
//		tasks = new ArrayList<Task>();
//		for(int i = 0; i < jsontasks.length(); i++)
//		{
//			tasks.add(new Task(jsontasks.getJSONObject(i),i,this));
//		}
//	}
	public TaskManager(ResourceProvider rp) {
		super(GetCommands());
		rp_ = rp;
	}
	String tasknew(JSONObject res)
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
//	@Override
//	public String getResultAndFormat(JSONObject res) throws Exception {
//		if(res.has("name"))
//		{
//			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
//			if(res.getString("name").compareTo("tasknew")==0)
//				//,{"name":"tasknew","args":[{"name":"estimate","type":"int"}]
//				return tasknew(res);
//			if(res.getString("name").compareTo("taskdone")==0)
//			{
//				//,{"name":"taskdone","args":[{"name":"taskid","type":"int"}]
//				tasks.get(res.getInt("taskid")).markDone();
//				return String.format("marked task #%d as done", res.getInt("taskid"));
//			}
//			if(res.getString("name").compareTo("tasks")==0)
//			{
//				//,{"name":"tasksshow","args":[],
//				if(res.has("tasknum"))
//					return this.getTasks(res.getInt("tasknum"));
//				else
//					return this.getTasks();
//				
//			}
//			if(res.getString("name").compareTo("taskpostpone")==0)
//			{
//				//,{"name":"taskpostpone",
//				//"args":[{"name":"taskid","type":"int"},{"name":"estimate","type":"int"}]
//				tasks.get(res.getInt("taskid")).postpone(res.getInt("estimate"));
//				return "postponed task #"+res.getInt("taskid")+" by "+res.getInt("estimate");
//			}
//		}
//		return null;
//	}
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
		.put(MakeCommand("taskdone","mark task as done",Arrays.asList(MakeCommandArg("taskid",ArgTypes.integer,false))))
		.put(MakeCommand("taskpostpone","postpone a task",
				Arrays.asList(MakeCommandArg("taskid",ArgTypes.integer,false),MakeCommandArg("estimate",ArgTypes.integer,false))))
		.put(MakeCommand("tasks","show list of tasks",Arrays.asList(MakeCommandArg("tasknum",ArgTypes.integer,true))))
		.put(MakeCommand("tasknew", "create new task",
				Arrays.asList(MakeCommandArg("estimate",ArgTypes.integer, false),
				MakeCommandArg("description",ArgTypes.remainder, true))));
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
