package managers.tasks;

import java.util.TimerTask;

public class TaskReminder extends TimerTask {
	protected boolean isDone_ = false;
	protected boolean isCancelled_ = false;
	Task task_ = null;
	TaskManagerForTask m_ = null;
	public TaskReminder(Task task, TaskManagerForTask m) {
		task_ = task;
		m_ = m;
	}
	@Override
	public void run() {
		if(isCancelled_ || task_.isDone())
			return;
		m_.sendMessage(this.getReminderMessage());
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
				task_.getIndex(),task_.getRemindBeforeMin());
	}
}
