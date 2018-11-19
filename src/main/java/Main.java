import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import assistantbot.MyAssistantBot;
import shell.InteractiveShell;
import util.Util;
import com.github.nailbiter.util.opts.Option;
import static com.github.nailbiter.util.opts.Option.ArgEnum;
import util.JsonUtil;
public class Main {
    public static void main(String[] args) {
    	JSONObject description = new JSONObject(), result = null;
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
    	
    	Util.SetJarFolder(profileObj.getString("RESFOLDER"));
    	Util.SetRebootFileName(profileObj.getString("TMPFILE"));
    	Util.SetRebootCommandFileName(profileObj.getString("CMDFILE"));
    	Util.SetTmpFolderName(profileObj.getString("TMPFOLDER"));
    	
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