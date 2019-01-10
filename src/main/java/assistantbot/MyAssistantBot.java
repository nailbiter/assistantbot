package assistantbot;
import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import managers.MyManager;
import util.KeyRing;
import util.MyBasicBot;
import util.SettingCollection;
import util.TelegramUtil;
import util.UserData;
import util.db.MongoUtil;

public class MyAssistantBot extends MyBasicBot {
	private JSONObject profileObj_;
	private MongoClient mongoClient_ = null;
	public MyAssistantBot(JSONObject profileObj) {
		try {
			profileObj_ = profileObj;
			util.StorageManager.init();
			mongoClient_ = MongoUtil.GetMongoClient(profileObj.getString("PASSWORD"));
			KeyRing.init(getBotUsername(),mongoClient_);
			if( !profileObj.has("MANAGERS") )
				initializeUserRecords();
		} catch(Exception e) {
			e.printStackTrace(System.out);
		}
	}
	private void initializeUserRecords() throws JSONException, Exception {
		MongoCollection<Document> coll = 
				MongoUtil.GetSettingCollection(mongoClient_, SettingCollection.USERS);
		JSONArray array = MongoUtil.GetJSONArrayFromDatabase(coll);
		for(Object o:array) {
			JSONObject obj = (JSONObject)o;
			System.err.format("checking whether initialize \"%s\"\n", obj.getString("name"));
			if(obj.has("port") && !obj.isNull("port")) {
				System.err.format("initialize %s\n", obj.toString(2));
				long port = (long) obj.getJSONObject("port").getInt("$numberLong");
				BasicUserData ud = new MyAssistantUserData(port,this,
						obj.getJSONArray("managers"),obj);
				userData.put(port, 
						(UserData)ud);
				if(obj.has("restartmessage")) {
					JSONArray restartmessage = obj.getJSONArray("restartmessage");
					StringBuilder sb = new StringBuilder();
					for(Object s:restartmessage)
						sb.append(((String)s)+"\n");
					ud.sendMessage(sb.toString());
				}
			}
		}
	}
	@Override
	public JSONObject interpret(Message msg, UserData ud) throws Exception {
		JSONObject res = new JSONObject();
		if(msg.hasDocument()) {
			System.out.println("we have document " + msg.getDocument());
			System.out.println("we got path " + TelegramUtil.getFilePath(msg.getDocument(), this));
			
			res.put("filepath",TelegramUtil.getFilePath(msg.getDocument(), this));
			res.put("filename", msg.getDocument().getFileName());
			return res;
		} else {
			res = ((MyAssistantUserData)ud).getParser().parse(msg.getText());
			return res;
		}
			
	}

	@Override
	public UserData createUserData(Long chatId) throws JSONException, Exception {
		return new MyAssistantUserData(chatId,this,
				profileObj_.optJSONArray("MANAGERS"));
	}

	@Override
	public String getResultAndFormat(JSONObject res,UserData ud) throws Exception {
		return ((MyAssistantUserData)ud).interpret(res);
	}

	@Override
	public String getBotUsername() {
		return (String)profileObj_.getString("NAME");
	}

	@Override
	public String getBotToken() {
		return util.KeyRing.getToken();
	}
	public MongoClient getMongoClient() {
		return mongoClient_;
	}
	public int sendFile(String fn, Long chatId) throws TelegramApiException {
		logger_.info(String.format("fn=%s", fn));
		SendDocument message = new SendDocument()
				.setChatId(chatId)
				.setDocument(new File(fn));
		return execute(message).getMessageId();
	}
	public int sendMessage(String msg, Long chatID_) {
		try 
		{
			SendMessage message = new SendMessage()
					.setChatId(chatID_)
							.setText(msg);
			return execute(message).getMessageId();
		}
		catch(Exception e){ 
			e.printStackTrace(System.out);
			return -1;
		}
	}
	/**
	 * 
	 * @param msg
	 * @param chatID_
	 * @param whom
	 * @param buttons
	 * @return message id
	 */
	public int sendMessageWithKeyBoard(String msg, Long chatID_, List<List<InlineKeyboardButton>> buttons) {
		try {
			SendMessage message = new SendMessage()
					.setChatId(chatID_)
							.setText(msg);
			
			InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
			markupInline.setKeyboard(buttons);
			message.setReplyMarkup(markupInline);
			Message res = execute(message); 
			int id = res.getMessageId();
			logger_.info(String.format("return id=%d", id));
			return id;
		} catch(Exception e) { 
			e.printStackTrace(System.out);
			return -1;
		}
	}
	@Override
	protected String interpretReply(int replyID, String message, UserData userData2) {
		MyAssistantUserData ud = (MyAssistantUserData) userData2;
		return ud.processReply(replyID, message);
	}
}