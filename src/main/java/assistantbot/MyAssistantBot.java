package assistantbot;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.telegrambots.api.objects.Message;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import managers.MyManager;
import util.KeyRing;
import util.MyBasicBot;
import util.UserData;
import util.Util;

public class MyAssistantBot extends MyBasicBot {
	private String botUserName_;
	public static MongoClient GetMongoClient(String password) {
		MongoClientURI uri = new MongoClientURI(
				String.format("mongodb://%s:%s@ds149672.mlab.com:49672/logistics", 
                        "nailbiter",password));
		return new MongoClient(uri);
	}
	public MyAssistantBot(Map<Character, Object> commandline)
	{
		try
		{
			util.StorageManager.init();
			botUserName_ = (String)commandline.get('n');
			mongoClient = GetMongoClient((String)commandline.get('p'));
			KeyRing.init(botUserName_,mongoClient);
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
			System.out.println("we got path " + Util.getFilePath(msg.getDocument(), this));
			
			res.put("filepath",Util.getFilePath(msg.getDocument(), this));
			res.put("filename", msg.getDocument().getFileName());
			return res;
		}
		else
		{
			res = ((MyAssistantUserData)ud).getParser().parse(msg.getText());
			/*if(msg.isReply())
				res.put("replyID", msg.getReplyToMessage().getMessageId());*/
			return res;
		}
			
	}

	@Override
	protected UserData createUserData(Long chatId) {
		return new MyAssistantUserData(chatId,this);
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
//		return "AssistantBot";
		return botUserName_;
	}

	@Override
	public String getBotToken() {
		return util.KeyRing.getToken();
	}
}