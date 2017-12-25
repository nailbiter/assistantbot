import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.telegrambots.api.objects.Message;

import util.Util;

public class MyAssistantBot extends MyBasicBot {
	java.io.ByteArrayOutputStream myByteStream = new java.io.ByteArrayOutputStream();
	util.Parser parser;
	MyAssistantBot()
	{
		try
		{
			util.StorageManager.init();
			jshell.Command.setCustomOut(myByteStream);
			parser = new util.Parser(util.LocalUtil.getJSONArrayFromRes(this, "parser"));
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
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
		return this.parser.parse(msg.getText());//res.put("cmd",msg.getText());
	}

	@Override
	UserData createUserData(Long chatId) {
		return new MyAssistantUserData(chatId,this);
	}

	@Override
	String getResultAndFormat(JSONObject res,UserData ud) throws Exception {
		if(res.has("filename"))
		{
			File file = Util.downloadPhotoByFilePath(res.getString("filepath"),this);
			String fn = "./"+res.getString("filename");
			File file2 = new File(fn);
			Util.copyFileUsingStream(file, file2);
			return "saved "+res.getString("filename");
		}
		if(res.has("cmd"))
		{
			if(((MyAssistantUserData)ud).isLocked())
				return "log in first";
			String out = this.myByteStream.toString();
			this.myByteStream.reset();
			if(out==null||out.length()==0)
				out = "null";
			System.out.println("out="+out+", len="+out.length());
			return out;
		}
		if(res.has("name"))
		{
			if(res.getString("name").compareTo("help")==0)
				return parser.getHelpMessage();
			if(res.getString("name").compareTo("habits")==0)
				return ((MyAssistantUserData)ud).getHabitManager().getHabitsInfo();
			if(res.getString("name").compareTo("done")==0) 
				return ((MyAssistantUserData)ud).getHabitManager().taskDone(res.getString("habit"));
			if(res.getString("name").compareTo("moneycats")==0) 
				return ((MyAssistantUserData)ud).getMoneyManager().getMoneyCats();
			if(res.getString("name").compareTo("money")==0) {
				((MyAssistantUserData)ud).getMoneyManager().putMoney(
						res.getInt("amount"), res.getString("category"));
				return String.format("put %d in category %s",
						res.getInt("amount"),res.getString("category"));
			}
			if(res.getString("name").equals("costs"))
				return ((MyAssistantUserData)ud).getMoneyManager().getLastCosts(
						res.getInt("num"));
		}
		return "unrecognized command";
	}

	@Override
	public String getBotUsername() {
		return "AssistantBot";
	}

	@Override
	public String getBotToken() {
		return util.KeyRing.getToken();
	}

}
