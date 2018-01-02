import org.json.JSONObject;

import it.sauronsoftware.cron4j.Scheduler;
import jshell.JShell;
import util.KeyRing;
import util.UserData;

public class MyAssistantUserData implements UserData {
	private HabitManager habitManager = null;
	private MoneyManager moneyManager = null;
	protected Scheduler scheduler = null; //FIXME: should it be a singleton?
	JShellManager jshellmanager = null;
	TimeManager timeManager = null;
	HabitManager getHabitManager() { return habitManager; }
	MoneyManager getMoneyManager() { return moneyManager; }
	JShellManager getJShellManager() { return this.jshellmanager; }
	TimeManager getTimeManager() {return this.timeManager; }
	MyAssistantUserData(Long chatID,MyAssistantBot bot){
		this.scheduler = new Scheduler();
		this.moneyManager = new MoneyManager(bot);
		this.habitManager = new HabitManager(chatID,bot,scheduler);
		this.timeManager = new TimeManager(chatID,bot,scheduler);
		try 
		{
			this.jshellmanager = new JShellManager(bot);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
		scheduler.start();
	}
	String lastCategory = null;
	public void Update(JSONObject res)  {
		if(res.has("name"))
		{
			if(res.getString("name").equals("money"))
			{
				if(!res.has("category"))
				{
					res.put("category", lastCategory);
				}
				lastCategory = res.getString("category");
			}
			if(res.getString("name").equals("costs"))
			{
				if(!res.has("num"))
				{
					res.put("num", 10);
				}
			}
		}
	}
}
