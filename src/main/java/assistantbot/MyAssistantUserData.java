package assistantbot;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import it.sauronsoftware.cron4j.Scheduler;
import managers.BadHabitManager;
import managers.MiscUtilManager;
import managers.MyManager;
import managers.OptionReplier;
import managers.TimeManager;
import util.LocalUtil;
import util.MyBasicBot;
import util.UserData;
import util.parsers.AbstractParser;

public class MyAssistantUserData extends UserData {
	protected Scheduler scheduler = null; //FIXME: should it be a singleton?
	protected static boolean ISBOTMANAGER = false;
	protected List<MyManager> managers = new ArrayList<MyManager>();
	List<MyManager> getManagers(){return managers;}
	protected AbstractParser parser = null;
	String lastCategory = null;
	TimeManager tm_ = null;
	long chatID_;
	MyBasicBot bot_ = null;
	private Logger logger_; 
	MyAssistantUserData(Long chatID,MyAssistantBot bot){
		try 
		{
			chatID_ = chatID;
			bot_ = bot;
			logger_ = Logger.getLogger(this.getClass().getName());
            System.out.println("password: "+bot.getDatabasePassword());
			MongoClientURI uri = new MongoClientURI(
					String.format("mongodb://%s:%s@ds149672.mlab.com:49672/logistics", 
                            "nailbiter",
							bot.getDatabasePassword()));
			MongoClient mongoClient = new MongoClient(uri);
//			DB database = mongoClient.getDB("test");
//			DBCollection collection = database.getCollection("time");
			
			if(!MyAssistantUserData.ISBOTMANAGER)
			{
				scheduler = new Scheduler();
				scheduler.setTimeZone(LocalUtil.getTimezone());
				managers.add(new managers.MoneyManager(this));
				managers.add(new managers.HabitManager(chatID,bot,scheduler,this));
				managers.add(new managers.TaskManager(chatID, bot));
				managers.add(new managers.TestManager(chatID, bot,scheduler,this));
				managers.add(tm_ = new managers.TimeManager(chatID,bot,scheduler,mongoClient,this));
				//managers.add(new managers.MailManager(chatID,bot,scheduler,this));
				managers.add(new MiscUtilManager());
				managers.add(new BadHabitManager(this));
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
	public boolean isSleeping() {return (tm_ != null) && tm_.isSleeping();}
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
	/**
	 * 
	 * @param msg
	 * @param categories
	 * @return message id
	 */
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
}
