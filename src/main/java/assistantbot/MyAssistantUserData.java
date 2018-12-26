package assistantbot;
import static util.parsers.StandardParserInterpreter.CMD;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.mongodb.MongoClient;
import it.sauronsoftware.cron4j.Scheduler;
import managers.MyManager;
import managers.OptionReplier;
import util.Util;
import util.UserData;
import util.parsers.AbstractParser;
import util.parsers.StandardParserInterpreter;

public class MyAssistantUserData extends UserData implements ResourceProvider {
	protected Scheduler scheduler_ = null; //FIXME: should it be a singleton?
	protected StandardParserInterpreter parser_ = null;
	protected long chatID_;
	MyAssistantBot bot_ = null;
	private Logger logger_;
	private List<MyManager> managers_ = new ArrayList<MyManager>();
	MyAssistantUserData(Long chatID,MyAssistantBot bot, JSONArray names){
		try {
			chatID_ = chatID;
			bot_ = bot;
			logger_ = Logger.getLogger(this.getClass().getName());
			

			scheduler_ = new Scheduler();
			scheduler_.setTimeZone(Util.getTimezone());
			parser_ = StandardParserInterpreter.Create(managers_, names,this);
		} catch(Exception e) {
			e.printStackTrace(System.out);
		}
		if(scheduler_!=null) 
			scheduler_.start();
	}
	public AbstractParser getParser() {return parser_;}
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
		for(int i = 0; i < managers_.size(); i++)
		{
			if(OptionReplier.class.isAssignableFrom(managers_.get(i).getClass()))
			{
				res.add((OptionReplier)managers_.get(i));
				System.out.format("adding %s\n", managers_.get(i).getClass().getName());
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
	public MongoClient getMongoClient() { 
		return bot_.getMongoClient(); 
	}
	@Override
	public int sendMessage(String msg) {
		return bot_.sendMessage(msg, chatID_);
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
	@Override
	public int sendFile(String fn) throws TelegramApiException {
		return bot_.sendFile(fn, chatID_);
	}
	public String interpret(JSONObject res) throws JSONException, Exception {
		return parser_.getDispatchTable().get(res.getString(CMD)).getResultAndFormat(res);
	}
	@Override
	public long getChatId() {
		return chatID_;
	}
}
