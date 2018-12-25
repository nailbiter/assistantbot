import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import assistantbot.MyAssistantBot;
import shell.InteractiveShell;
import testing.MainTest;
import util.Util;
import util.AssistantBotException;
import util.JsonUtil;
public class Main {
    public static void main(String[] args) throws Exception {
    	try {
    		new MainTest().test();
    	} catch (Exception e) {
    		e.printStackTrace(System.out);
    		throw e;
    	}
    	
    	
    	JSONObject description = new JSONObject(), 
    			result = null;
    	
    	try {
			result = new JSONObject(com.github.nailbiter.util.Util.ParseCommandLine(description.toString(), args));
			if(result.getJSONArray("others").length()>0) {
				JSONArray others = result.getJSONArray("others");
				for(int i = 0; i < others.length(); i++) {
					String fn = others.getString(i);
					if(fn==null || !fn.endsWith(".json"))
						continue;
					JSONObject obj = new JSONObject(Util.GetFile(fn));
					JsonUtil.CopyIntoJson(result.getJSONObject("keys"), obj);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
    	JSONObject profileObj =  result.getJSONObject("keys");
    	JsonUtil.CapitalizeJsonKeys(profileObj);
    	System.err.format("profile: %s\n",profileObj.toString(2));
    	Util.setProfileObj(profileObj.toString());
    	
    	if(profileObj.has("OFFLINE")) {
    		try {
				InteractiveShell.Start(profileObj);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
    	} else {
            ApiContextInitializer.init();
            System.out.println("here I go!");
            TelegramBotsApi botsApi = new TelegramBotsApi();

            try {
                botsApi.registerBot(new MyAssistantBot(profileObj));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
    	}
    }
}