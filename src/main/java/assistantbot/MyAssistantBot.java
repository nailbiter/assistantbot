package assistantbot;
import java.util.List;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Message;

import managers.MyManager;
import util.KeyRing;
import util.MongoUtil;
import util.MyBasicBot;
import util.UserData;
import util.TelegramUtil;

public class MyAssistantBot extends MyBasicBot {
	private String botUserName_;
	private JSONObject profileObj_;
	public MyAssistantBot(JSONObject profileObj)
	{
		try
		{
			profileObj_ = profileObj;
			util.StorageManager.init();
			botUserName_ = (String)profileObj.getString("NAME");
			mongoClient_ = MongoUtil.GetMongoClient(profileObj.getString("PASSWORD"));
			KeyRing.init(botUserName_,mongoClient_);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
	@Override
	protected JSONObject parse(Message msg, UserData ud) throws Exception {
		JSONObject res = new JSONObject();
		if(msg.hasDocument())
		{
			System.out.println("we have document " + msg.getDocument());
			System.out.println("we got path " + TelegramUtil.getFilePath(msg.getDocument(), this));
			
			res.put("filepath",TelegramUtil.getFilePath(msg.getDocument(), this));
			res.put("filename", msg.getDocument().getFileName());
			return res;
		}
		else
		{
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
		MyAssistantUserData md = (MyAssistantUserData)ud;
		String str= null;
		List<MyManager> managers = md.getManagers();
		for(int i = 0; i < managers.size(); i++)
			if((str = managers.get(i).getResultAndFormat(res))!=null)
				return str;
		throw new Exception("unrecognized command");
	}

	@Override
	public String getBotUsername() {
		return botUserName_;
	}

	@Override
	public String getBotToken() {
		return util.KeyRing.getToken();
	}
}