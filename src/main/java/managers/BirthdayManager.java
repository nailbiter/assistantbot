package managers;

import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import it.sauronsoftware.cron4j.Scheduler;

public class BirthdayManager extends AbstractManager implements Runnable{
	private static final String BIRTHDAYS = "birthdays";
	private static final String SCHEDULINGPATTERN = "0 10 * * *";
	private static final String MONTH = "month";
	private static final String DATE = "date";
	private static final String ENABLED = "enabled";
	private static final String INFO = "info";
	private ResourceProvider rp_;

	public BirthdayManager(ResourceProvider rp) throws JSONException, Exception {
		super(new JSONArray());
		rp_ = rp;
		ScheduleBirthdays(rp.getScheduler(),this);
		run();
	}

	private static void ScheduleBirthdays(Scheduler scheduler, BirthdayManager bm) {
		if(scheduler==null)
			System.err.format("sched null\n");
		if(bm==null)
			System.err.format("bm null\n");
		scheduler.schedule(SCHEDULINGPATTERN, bm);
	}

	@Override
	public void run() {
		try {
			realRun();
		} catch (Exception e) {
			rp_.sendMessage(String.format("%s e: %s"
					,this.getClass().getSimpleName()
					,ExceptionUtils.getStackTrace(e)));
		}
	}

	private void realRun() throws JSONException, Exception {
		JSONArray birthdays = this.getParamObject(rp_).getJSONArray(BIRTHDAYS);
		Date now = new Date();
		for(Object o:birthdays)
			tryToCongratulate((JSONObject) o,now);
	}

	private void tryToCongratulate(JSONObject obj, Date now) {
		if( now.getMonth()==(obj.getInt(MONTH)-1) && now.getDate()==obj.getInt(DATE) && obj.optBoolean(ENABLED,true)) {
			rp_.sendMessage(String.format("don't forget to congratulate %s!%s"
					, obj.getString("name")
					, obj.has(INFO)?String.format("\n%s",obj.getString(INFO)):""));
		}
	}
}
