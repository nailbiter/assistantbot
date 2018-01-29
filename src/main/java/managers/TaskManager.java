/**
 * 
 */
package managers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import util.LocalUtil;
import util.MyBasicBot;
import util.StorageManager;

/**
 * @author nailbiter
 *
 */
public class TaskManager implements MyManager {
	protected Long chatID_ = null;
	protected MyBasicBot bot_ = null;
	protected List<Task> tasks = null;
	protected JSONArray jsontasks = null;
	protected static int REMINDBEFOREMIN = 10;
	Timer timer = new Timer();
	protected class Task
	{
		JSONObject obj_ = null;
		TimerTask tt = null;
		int index_;
		Task(int estimate)
		{
			obj_ = new JSONObject()
					.put("done", false)
					.put("history", new JSONArray()
							.put((new Date()).getTime())
							.put(estimate));
			jsontasks.put(obj_);
			index_ = jsontasks.length() - 1;
			updateReminder();
		}
		Task(JSONObject obj,int index){ obj_ = obj; updateReminder(); index_ = index;}
		void markDone() 
		{ 
			obj_.put("done", true);
			obj_.getJSONArray("history").put((new Date()).getTime())
										.put("done");
			updateReminder();
		}
		boolean isDone() { return obj_.getBoolean("done");}
		String getDuedate() throws Exception
		{
			JSONArray history = obj_.getJSONArray("history");
			Long date = history.getLong(history.length() - 2);
			Date lastDate = new Date(date);
			int timeInMin = history.getInt(history.length() - 1);
			//return (new Date(lastDate.getTime() + timeInMin*60*1000)).toString();
			return LocalUtil.DateToString((new Date(lastDate.getTime() + timeInMin*60*1000)));
		}
		void postpone(int byhowmuch)
		{
			JSONArray history = obj_.getJSONArray("history");
			obj_.put("done", false);
			history.put((new Date()).getTime());
			history.put(byhowmuch);
			updateReminder();
		}
		String getRemainingTime()
		{
			Long curTime = (new Date()).getTime();
			JSONArray history = obj_.getJSONArray("history");
			Long date = history.getLong(history.length() - 2);
			int timeInMin = history.getInt(history.length() - 1);
			return util.LocalUtil.milisToTimeFormat(date+timeInMin*60*1000-curTime);
		}
		void updateReminder()
		{
			if(isDone())
				return;
			if(tt!=null)
				tt.cancel();
			tt = new TaskReminder(index_);
			
			JSONArray history = obj_.getJSONArray("history");
			Long date = history.getLong(history.length() - 2);
			int timeInMin = history.getInt(history.length() - 1);
			
			timer.schedule(tt, new Date(date + (timeInMin - this.getRemindBeforeMin())*60*1000));
		}
		int getRemindBeforeMin()
		{
			if(isDone())
				return 0;
			JSONArray history = obj_.getJSONArray("history");
			int timeInMin = history.getInt(history.length() - 1);
			return (timeInMin / 10);
		}
		public String getCompletionDate() throws Exception{
			if(!isDone())
				throw new Exception(String.format("task #%d is not completed!", this.index_));
			
			JSONArray history = obj_.getJSONArray("history");
			Long completionTime = history.getLong(history.length()-2);
			return (new Date(completionTime)).toString();
		}
	}
	protected class TaskReminder extends TimerTask
	{
		protected boolean isDone_ = false;
		protected boolean isCancelled_ = false;
		protected int index_;
		TaskReminder(int index)
		{
			index_ = index;
		}
		@Override
		public void run() {
			if(isCancelled_ || tasks.get(index_).isDone())
				return;
			bot_.sendMessage(this.getReminderMessage(), chatID_);
			isDone_ = true;
			return;
		}
		@Override
		public boolean cancel() 
		{
			isCancelled_ = true;
			return !isDone_;
		}
		protected String getReminderMessage()
		{
			return String.format("task #%d is due in %dmin!", 
					index_,tasks.get(index_).getRemindBeforeMin());
		}
	}
	public TaskManager(Long chatID, MyBasicBot bot)
	{
		chatID_ = chatID;
		bot_ = bot;
		JSONObject obj = StorageManager.get("tasks", true);
		if(!obj.has("tasks"))
			obj.put("tasks", new JSONArray());
		jsontasks = obj.getJSONArray("tasks");
		tasks = new ArrayList<Task>();
		for(int i = 0; i < jsontasks.length(); i++)
			tasks.add(new Task(jsontasks.getJSONObject(i),i));
	}

	/* (non-Javadoc)
	 * @see util.MyManager#getResultAndFormat(org.json.JSONObject)
	 */
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("tasknew")==0)
			{
				//,{"name":"tasknew","args":[{"name":"estimate","type":"int"}]
				Task task = new Task(res.getInt("estimate"));
				tasks.add(task);
				return String.format("schedule task #%d to be exec within %d min", 
						tasks.size()-1,res.getInt("estimate"));
			}
			if(res.getString("name").compareTo("taskdone")==0)
			{
				//,{"name":"taskdone","args":[{"name":"taskid","type":"int"}]
				tasks.get(res.getInt("taskid")).markDone();
				return "marked task #"+res.getInt("taskid")+" as done";
			}
			if(res.getString("name").compareTo("tasks")==0)
			{
				//,{"name":"tasksshow","args":[],
				if(res.has("tasknum"))
					return this.getTasks(res.getInt("tasknum"));
				else
					return this.getTasks();
				
			}
			if(res.getString("name").compareTo("taskpostpone")==0)
			{
				//,{"name":"taskpostpone",
				//"args":[{"name":"taskid","type":"int"},{"name":"estimate","type":"int"}]
				tasks.get(res.getInt("taskid")).postpone(res.getInt("estimate"));
				return "postponed task #"+res.getInt("taskid")+" by "+res.getInt("estimate");
			}
		}
		return null;
	}
	private String getTasks(int tasknum) throws Exception{
		util.TableBuilder tb = new util.TableBuilder();
		tb.addNewlineAndTokens("#", "completion date");
		int index = tasks.size() - 1;
		while(tasknum>0 && index >=0)
		{
			if(tasks.get(index).isDone())
			{
				tb.newRow();
				tb.addToken(index);
				tb.addToken(tasks.get(index).getCompletionDate());
				tasknum--;
			}
			index--;
		}
		return tb.toString();
	}

	String getTasks () throws Exception
	{
		util.TableBuilder tb = new util.TableBuilder();
		tb.addNewlineAndTokens("#", "duedate","minutes till");
		
		for(int i = 0; i < tasks.size(); i++)
			if(!tasks.get(i).isDone())
			{
				tb.newRow();
				tb.addToken(i);
				tb.addToken(tasks.get(i).getDuedate());
				tb.addToken(tasks.get(i).getRemainingTime());
			}
		return tb.toString();
	}

	@Override
	public JSONArray getCommands() {
		return new JSONArray("[{\"name\":\"tasknew\",\"args\":[{\"name\":\"estimate\",\"type\":\"int\"}],\"help\":\"create new task\"}\n" + 
				",{\"name\":\"taskdone\",\"args\":[{\"name\":\"taskid\",\"type\":\"int\"}],\"help\":\"mark task as done\"}\n" + 
				",{\"name\":\"tasks\",\"args\":[{\"name\":\"tasknum\",\"type\":\"int\",\"isOpt\":true}],\"help\":\"show list of tasks\"}\n" + 
				",{\"name\":\"taskpostpone\",\"args\":[{\"name\":\"taskid\",\"type\":\"int\"},{\"name\":\"estimate\",\"type\":\"int\"}],\"help\":\"postpone a task\"}]");
	}

	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}

}
