package assistantbot;
import static util.parsers.StandardParserInterpreter.CMD;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;

import ch.qos.logback.core.filter.Filter;
import it.sauronsoftware.cron4j.Scheduler;
import managers.MyManager;
import managers.OptionReplier;
import util.Util;
import util.UserData;
import util.parsers.AbstractParser;
import util.parsers.StandardParserInterpreter;

public class MyAssistantUserData extends UserData implements ResourceProvider,MyManager {
	protected Scheduler scheduler_ = null; //FIXME: should it be a singleton?
	protected StandardParserInterpreter parser_ = null;
	protected long chatID_;
	MyAssistantBot bot_ = null;
	private Logger logger_;
	private List<MyManager> managers_ = new ArrayList<MyManager>();
	private String userName_ = null;
	private static final String DEFAULTUSERNAME = "alex";
	public static final String LOGISTICS = "logistics";
	MyAssistantUserData(Long chatID,MyAssistantBot bot, JSONArray names){
		this(chatID,bot,names,null);
	}
	MyAssistantUserData(Long chatID,MyAssistantBot bot, JSONArray names,String name){
		try {
			chatID_ = chatID;
			bot_ = bot;
			logger_ = Logger.getLogger(this.getClass().getName());
			scheduler_ = new Scheduler();
			scheduler_.setTimeZone(Util.getTimezone());
			managers_.add(this);
			parser_ = StandardParserInterpreter.Create(managers_, names, this);
			userName_ = (name != null)? 
					name
					:(( names == null ) ? 
							null 
							: DEFAULTUSERNAME
							);
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
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	@Override
	public JSONObject getCommands() {
		return new JSONObject()
				.put("help", "display this message")
				.put("start", "display this message")
				;
	}
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has(CMD))
		{
			System.err.println(this.getClass().getName()+" got comd: "+res.getString(CMD));
			if(res.getString(CMD).equals("help"))
				return parser_.getHelpMessage();
			else if(res.getString(CMD).equals("start")) {
				String[] split = res.optString(StandardParserInterpreter.REM,"")
						.split(StandardParserInterpreter.SPLITPATTERN);
				if( split.length==0 || split[0].isEmpty() ) {
					return unlogin();
				} else {
					String username = split[0],
							password = (split.length>=2) ? split[1] : "";
					return login(username,password);
				}
			}
		}
		throw new Exception(String.format("for res=%s", res.toString()));
	}
	private String login(String username, String password) throws JSONException, Exception {
		Document userDoc = new Document("name",username);
		userDoc.put("pass", password);
		MongoCollection<Document> coll = bot_.getMongoClient()
				.getDatabase(LOGISTICS).getCollection("users");
		Document doc = coll.find(userDoc).first();
		if( doc == null ) {
			return String.format("cannot log in \"%s\" (wrong username or password)", 
					username);
		} else if(doc.containsKey("port")) {
			return String.format("\"%s\" is already logged in", doc.getString("name"));
		}
		
		JSONObject obj = new JSONObject(doc.toJson());
		userName_ = obj.getString("name");
		parser_ = StandardParserInterpreter
				.Create(managers_, obj.getJSONArray("managers"), this);
		coll.findOneAndUpdate(userDoc, Updates.set("port",getChatId()));
		
		StringBuilder sb = new StringBuilder();
		for(Object o:obj.getJSONArray("loginmessage"))
			sb.append(((String)o)+"\n");
		return sb.toString();
	}
	private String unlogin() throws JSONException, Exception {
		if( userName_ == null ) {
			return String.format("not logged in");
		}
		MongoCollection<Document> coll = bot_.getMongoClient()
				.getDatabase(LOGISTICS).getCollection("users");
		
		coll.findOneAndUpdate(new Document("name", userName_),Updates.unset("port"));
		managers_.clear();
		managers_.add(this);
		parser_ = StandardParserInterpreter.Create(managers_, null, this);
		String res = String.format("logging out \"%s\"",userName_);
		userName_ = null;
		
		return res;
	}
	@Override
	public String getDbName() {
		if( userName_.equals(DEFAULTUSERNAME) )
			return "logistics";
		else
			return userName_;
	}
}
