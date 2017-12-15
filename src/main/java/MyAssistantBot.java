import java.io.File;

import org.json.JSONObject;
import org.telegram.telegrambots.api.objects.Message;

import util.Util;

public class MyAssistantBot extends MyBasicBot {
	java.io.ByteArrayOutputStream myByteStream = new java.io.ByteArrayOutputStream();
	MyAssistantBot()
	{
		jshell.Command.setCustomOut(myByteStream);
	}
	@Override
	JSONObject parse(Message msg, UserData ud) throws Exception {
		JSONObject res = new JSONObject();
		if(msg.hasDocument())
		{
			System.out.println("we have document " + msg.getDocument());
			System.out.println("we got path " + Util.getFilePath(msg.getDocument(), this));
			
			res.put("filepath",Util.getFilePath(msg.getDocument(), this));
			res.put("filename", msg.getDocument().getFileName());
			return res;
		}
		return res.put("cmd",msg.getText());
	}

	@Override
	UserData createUserData(Long chatId) {
		return new MyAssistantUserData();
	}

	@Override
	String getResultAndFormat(JSONObject res) throws Exception {
		//return res.getString("msg");
		if(res.has("filename"))
		{
			File file = Util.downloadPhotoByFilePath(res.getString("filepath"),this);
			String fn = "./"+res.getString("filename");
			File file2 = new File(fn);
			Util.copyFileUsingStream(file, file2);
			return "saved "+res.getString("filename");
		}
		String out = this.myByteStream.toString();
		this.myByteStream.reset();
		if(out==null||out.length()==0)
			out = "null";
		System.out.println("out="+out+", len="+out.length());
		return out;
	}

	@Override
	public String getBotUsername() {
		// TODO Auto-generated method stub
		return "AssistantBot";
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		return util.KeyRing.getToken();
	}

}
