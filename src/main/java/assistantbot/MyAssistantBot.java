package assistantbot;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.mongodb.MongoClient;

import util.KeyRing;
import util.MongoUtil;
import util.MyBasicBot;
import util.TelegramUtil;
import util.UserData;

public class MyAssistantBot extends MyBasicBot {
	private JSONObject profileObj_;
	protected MongoClient mongoClient_ = null;
	public MyAssistantBot(JSONObject profileObj) {
		try {
			profileObj_ = profileObj;
			util.StorageManager.init();
			mongoClient_ = MongoUtil.GetMongoClient(profileObj.getString("PASSWORD"));
			KeyRing.init(getBotUsername(),mongoClient_);
		} catch(Exception e) {
			e.printStackTrace(System.out);
		}
	}
	@Override
	protected JSONObject interpret(Message msg, UserData ud) throws Exception {
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
	protected UserData createUserData(Long chatId) {
		return new MyAssistantUserData(chatId,this,profileObj_.getJSONArray("MANAGERS"));
	}

	@Override
	protected String getResultAndFormat(JSONObject res,UserData ud) throws Exception {
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
}