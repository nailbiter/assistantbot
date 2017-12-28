import org.json.JSONObject;

import it.sauronsoftware.cron4j.Scheduler;
import util.MyManager;

/**
 * 
 */

/**
 * @author nailbiter
 *
 */
public class TimeManager implements MyManager {
	Scheduler scheduler_;
	Long chatID_;
	MyAssistantBot bot_;
	/* (non-Javadoc)
	 * @see util.MyManager#getResultAndFormat(org.json.JSONObject)
	 */
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	public TimeManager(Long chatID,MyAssistantBot bot,Scheduler scheduler_in) {
		this.scheduler_ = scheduler_in;
		this.chatID_ = chatID;
		this.bot_ = bot;
	}

}
