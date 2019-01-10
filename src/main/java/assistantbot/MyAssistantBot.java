package assistantbot;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

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
	protected UserData createUserData(Long chatId) throws JSONException, Exception {
		return new MyAssistantUserData(chatId,this,
				profileObj_.optJSONArray("MANAGERS"));
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