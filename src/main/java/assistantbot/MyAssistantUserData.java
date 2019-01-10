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

import it.sauronsoftware.cron4j.Scheduler;
import managers.MyManager;
import managers.OptionReplier;
import util.Util;
import util.db.MongoUtil;
import util.SettingCollection;
import util.UserCollection;
import util.UserData;
import util.parsers.AbstractParser;
import util.parsers.StandardParserInterpreter;

public class MyAssistantUserData extends BasicUserData implements UserData, ResourceProvider{
	protected long chatID_;
	MyAssistantBot bot_ = null;
	MyAssistantUserData(Long chatID,MyAssistantBot bot, JSONArray names) throws JSONException, Exception{
		this(chatID,bot,names,null);
	}
	MyAssistantUserData(Long chatID,MyAssistantBot bot, JSONArray names,JSONObject obj) throws JSONException, Exception{
		super(false);
		try {
			chatID_ = chatID;
			bot_ = bot;
			scheduler_ = new Scheduler();
			scheduler_.setTimeZone(Util.getTimezone());
			userObject_ = (obj != null)? 
					obj
					:(( names == null ) ? 
							null 
							: Util.GetDefaultUser()
							);
			
			managers_.add(this);
			parser_ = StandardParserInterpreter.Create(managers_, names, this);
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
//	@Override
//	public Scheduler getScheduler() {
//		return scheduler_;
//	}
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
	public String processReply(int messageID, String msg) {
		return null;
	}
	@Override
	protected String login(String username, String password) throws JSONException, Exception {
		Document userDoc = new Document("name",username);
		userDoc.put("pass", password);
		MongoCollection<Document> coll =
				MongoUtil.GetSettingCollection(
				bot_.getMongoClient()
				, SettingCollection.USERS);
		Document doc = coll.find(userDoc).first();
		if( doc == null ) {
			return String.format("cannot log in \"%s\" (wrong username or password)", 
					username);
		} else if(doc.containsKey("port")) {
			return String.format("\"%s\" is already logged in", doc.getString("name"));
		}
		
		JSONObject obj = new JSONObject(doc.toJson());
		userObject_ = 
				obj;
		parser_ = StandardParserInterpreter
				.Create(managers_, obj.getJSONArray("managers"), this);
		coll.findOneAndUpdate(userDoc, Updates.set("port",chatID_));
		
		StringBuilder sb = new StringBuilder();
		for(Object o:obj.getJSONArray("loginmessage"))
			sb.append(((String)o)+"\n");
		sendMessage(sb.toString());
		return "";
	}
	@Override
	protected String unlogin() throws JSONException, Exception {
		if( userObject_ == null ) {
			return String.format("not logged in");
		}
		MongoCollection<Document> coll =
				MongoUtil.GetSettingCollection(
				bot_.getMongoClient()
				,SettingCollection.USERS);
		
		coll.findOneAndUpdate(new Document("name", userObject_),Updates.unset("port"));
		managers_.clear();
		managers_.add(this);
		parser_ = StandardParserInterpreter.Create(managers_, null, this);
		String res = String.format("logging out \"%s\"",userObject_);
		userObject_ = null;
		
		return res;
	}
	@SuppressWarnings("deprecation")
	@Override
	public MongoCollection<Document> getCollection(UserCollection name) {
		return bot_.getMongoClient().getDatabase(MongoUtil.getLogistics())
				.getCollection(String
						.format("%s.%s", 
								userObject_.getString(Util.NAMEFIELDNAME),
								name.toString()));
	}
//	@Override
//	public JSONObject getUserObject() {
//		return userObject_;
//	}
}
