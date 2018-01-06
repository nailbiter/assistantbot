import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import it.sauronsoftware.cron4j.Scheduler;
import util.MyManager;
import util.UserData;
import util.parsers.AbstractParser;

public class MyAssistantUserData implements UserData {
	protected Scheduler scheduler = null; //FIXME: should it be a singleton?
	protected List<MyManager> managers = null;
	List<MyManager> getManagers(){return managers;}
	protected AbstractParser parser = null;
	String lastCategory = null;
	MyAssistantUserData(Long chatID,MyAssistantBot bot){
		try 
		{
			managers = new ArrayList<MyManager>();
			
			managers.add(new managers.JShellManager(bot));
			
			if(true)
			{
				parser = new util.parsers.Parser(util.LocalUtil.getJSONArrayFromRes(this, "parser"));
				this.scheduler = new Scheduler();
				managers.add(new managers.MoneyManager(bot));
				managers.add(new managers.HabitManager(chatID,bot,scheduler));
				managers.add(new managers.TimeManager(chatID,bot,scheduler));
				managers.add(new managers.MailManager(chatID,bot,scheduler));
				managers.add(new managers.TaskManager(chatID, bot));
			}
			else
				parser = new util.parsers.BotManagerParser();
			
			managers.add(parser);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
		if(scheduler!=null) scheduler.start();
	}
	public AbstractParser getParser() {return parser;}
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
