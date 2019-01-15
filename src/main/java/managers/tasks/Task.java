package managers.tasks;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.MyBasicBot;
import managers.TaskManager;
import util.Util;

public class Task
{
	JSONObject obj_ = null;
	TimerTask tt = null;
	int index_;
	TaskManagerForTask m_ = null;
	public Task(int index, TaskManagerForTask m) {
		index_ = index;
		m_ = m;
		obj_ = new JSONObject()
			.put("done", false)
			.put("history", new JSONArray());
	}
	public void setInitialEstimate(int estimate)
	{
		getHistory()
		.put((new Date()).getTime())
		.put(estimate);
		updateReminder();
	}
	public Task(JSONObject obj,int index,TaskManagerForTask m){
		this(index,m);
		obj_ = obj;
		updateReminder(); 
	}
	public JSONObject getJSONObject() {return obj_; }
	public void markDone(){ 
		obj_.put("done", true);
		obj_.getJSONArray("history")
			.put((new Date()).getTime())
			.put("done");
		updateReminder();
	}
	public boolean isDone() { return obj_.getBoolean("done");}
	public String getDuedate() throws Exception{
		JSONArray history = obj_.getJSONArray("history");
		if(history.length() > 0)
		{
			Long date = history.getLong(history.length() - 2);
			Date lastDate = new Date(date);
			int timeInMin = history.getInt(history.length() - 1);
			return Util.DateToString((new Date(lastDate.getTime() + timeInMin*60*1000)));
		}
		else
		{
			return "∞";
		}
	}
	public void postpone(int byhowmuch){
		JSONArray history = obj_.getJSONArray("history");
		obj_.put("done", false);
		history.put((new Date()).getTime());
		history.put(byhowmuch);
		updateReminder();
	}
	JSONArray getHistory() { return obj_.getJSONArray("history"); }
	public String getRemainingTime(){
		Long curTime = (new Date()).getTime();
		JSONArray history = getHistory();
		if(history.length() > 0)
		{
			Long date = history.getLong(history.length() - 2);
			int timeInMin = history.getInt(history.length() - 1);
			return util.Util.milisToTimeFormat(date+timeInMin*60*1000-curTime);
		}
		else
		{
			return "∞";
		}
	}
	void updateReminder(){
		if(isDone())
			return;
		if(tt!=null)
			tt.cancel();
		tt = new TaskReminder(this,m_);
		
		JSONArray history = this.getHistory();
		if( history.length() == 0 )
			return;
		Long date = history.getLong(history.length() - 2);
		int timeInMin = history.getInt(history.length() - 1);
		
		m_.schedule(tt, new Date(date + (timeInMin - this.getRemindBeforeMin())*60*1000));
	}
	int getRemindBeforeMin(){
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
	int getIndex() { return index_; }
	public void setDescription(String description) {
		obj_.put("description", description);
	}
	public String getDescription() {
		// TODO Auto-generated method stub
		return obj_.optString("description","<no descr>");
	}
}