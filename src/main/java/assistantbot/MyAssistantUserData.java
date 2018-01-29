package assistantbot;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import it.sauronsoftware.cron4j.Scheduler;
import managers.BadHabitManager;
import managers.MiscUtilManager;
import managers.OptionReplier;
import managers.SleepManager;
import util.LocalUtil;
import util.MyManager;
import util.UserData;
import util.parsers.AbstractParser;

public class MyAssistantUserData extends UserData {
	protected Scheduler scheduler = null; //FIXME: should it be a singleton?
	protected static boolean ISBOTMANAGER = false;
	protected List<MyManager> managers = new ArrayList<MyManager>();
	List<MyManager> getManagers(){return managers;}
	protected AbstractParser parser = null;
	String lastCategory = null;
	SleepManager sm_ = null;
	MyAssistantUserData(Long chatID,MyAssistantBot bot){
		try 
		{
			if(!MyAssistantUserData.ISBOTMANAGER)
			{
				scheduler = new Scheduler();
				scheduler.setTimeZone(LocalUtil.getTimezone());
				managers.add(new managers.MoneyManager(bot));
				managers.add(new managers.HabitManager(chatID,bot,scheduler));
				managers.add(new managers.TaskManager(chatID, bot));
				managers.add(new managers.TestManager(chatID, bot,scheduler));
				managers.add(sm_ = new managers.SleepManager(bot));
				managers.add(new managers.TimeManager(chatID,bot,scheduler,this));
				managers.add(new managers.MailManager(chatID,bot,scheduler,this));
				managers.add(new MiscUtilManager());
				managers.add(new BadHabitManager());
			}
			managers.add(util.StorageManager.getMyManager());
			managers.add(new managers.JShellManager(bot));
			
			if(MyAssistantUserData.ISBOTMANAGER)
				parser = new util.parsers.BotManagerParser();
			else
				parser = new util.parsers.StandardParser(managers);
			
			managers.add(parser);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
		if(scheduler!=null) scheduler.start();
	}
	public boolean isSleeping() {return (sm_ != null) && sm_.isSleeping();}
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
	@Override
	public List<OptionReplier> getOptionRepliers()
	{
		ArrayList<OptionReplier> res = new ArrayList<OptionReplier>();
		for(int i = 0; i < managers.size(); i++)
		{
			if(OptionReplier.class.isAssignableFrom(managers.get(i).getClass()))
			{
				res.add((OptionReplier)managers.get(i));
				System.out.format("adding %s\n", managers.get(i).getClass().getName());
			}
		}
		return res;
	}
}
