package assistantbot;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.mongodb.MongoClient;
import it.sauronsoftware.cron4j.Scheduler;
import managers.BadHabitManager;
import managers.GermanManager;
import managers.GymManager;
import managers.MiscUtilManager;
import managers.MyManager;
import managers.OptionReplier;
import managers.ReportManager;
import managers.TimeManager;
import util.Util;
import util.MyBasicBot;
import util.UserData;
import util.parsers.AbstractParser;

public class MyAssistantUserData extends UserData implements ResourceProvider {
	protected Scheduler scheduler_ = null; //FIXME: should it be a singleton?
	protected static boolean ISBOTMANAGER = false;
	protected List<MyManager> managers = new ArrayList<MyManager>();
	List<MyManager> getManagers(){return managers;}
	protected AbstractParser parser = null;
	String lastCategory = null;
	protected long chatID_;
	MyBasicBot bot_ = null;
	private Logger logger_; 
	MyAssistantUserData(Long chatID,MyBasicBot bot){
		try 
		{
			chatID_ = chatID;
			bot_ = bot;
			logger_ = Logger.getLogger(this.getClass().getName());
			
			if(!MyAssistantUserData.ISBOTMANAGER)
			{
				scheduler_ = new Scheduler();
				scheduler_.setTimeZone(Util.getTimezone());
				managers.add(new managers.MoneyManager(this));
				managers.add(new managers.HabitManager(this));
				managers.add(new managers.TaskManager(this));
				managers.add(new managers.TestManager(this));
				managers.add(new managers.TimeManager(this));
				managers.add(new MiscUtilManager(this));
				managers.add(new ReportManager(this));
				managers.add(new GermanManager(this));
				managers.add(new GymManager(this));
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
		if(scheduler_!=null) 
			scheduler_.start();
	}
//	public boolean isSleeping() {return (tm_ != null) && tm_.isSleeping();}
	public AbstractParser getParser() {return parser;}
	public void Update(JSONObject res)  {
		if(res.has("name"))
		{
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
	public String processUpdateWithCallbackQuery(String call_data, int message_id) throws Exception{
		String res = null;
		List<OptionReplier> repliers = this.getOptionRepliers();
		System.out.format("got %d repliers\n", repliers.size());
		for(int i = 0; i < repliers.size(); i++)
			if( (res = repliers.get(i).optionReply(call_data, message_id)) != null)
				return res;
		return res;
	}
	private List<OptionReplier> getOptionRepliers()
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
	@Override
	public int sendMessageWithKeyBoard(String msg, JSONArray categories)
	{
		final int ROWNUM = 2;
		logger_.info(String.format("categories=%s", categories.toString()));
		
		List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
		for(int i = 0; i < categories.length();)
		{
			buttons.add(new ArrayList<InlineKeyboardButton>());
			for(int j = 0; j < ROWNUM && i < categories.length(); j++)
			{
				buttons.get(buttons.size()-1).add(new InlineKeyboardButton()
						.setText(categories.getString(i))
						.setCallbackData(categories.getString(i)));
				i++;
			}
		}
		
		return bot_.sendMessageWithKeyBoard(msg, chatID_, buttons);
	}
	@Override
	public MongoClient getMongoClient() { return bot_.getMongoClient(); }
	@Override
	public void sendMessage(String msg) {
		bot_.sendMessage(msg, chatID_);
	}
	@Override
	public Scheduler getScheduler() {
		return scheduler_;
	}
	@Override
	public int sendMessage(String msg, MyManager whom) throws Exception {
		return bot_.sendMessage(msg, chatID_, whom);
	}
	@Override
	public int sendMessageWithKeyBoard(String msg, List<List<InlineKeyboardButton>> buttons) {
		return bot_.sendMessageWithKeyBoard(msg, chatID_, buttons);
	}
}
