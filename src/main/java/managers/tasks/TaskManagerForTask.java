package managers.tasks;

import java.util.Date;
import java.util.TimerTask;

public interface TaskManagerForTask {
	public void sendMessage(String msg);
	void schedule(TimerTask tt, Date d);
}
