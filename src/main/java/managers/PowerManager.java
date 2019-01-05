package managers;

import static util.Util.GetRebootFileName;

import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import util.Util;
import util.parsers.ParseOrderedCmd;

public class PowerManager extends AbstractManager {
	private ResourceProvider rp_;

	public PowerManager(ResourceProvider rp) throws Exception {
		super(GetCommands());
		rp_ = rp;
	}

	private static JSONArray GetCommands() {
		return new JSONArray()
				.put(new ParseOrderedCmd("exit", "exit the bot"))
				.put(new ParseOrderedCmd("restart","restart the bot"))
				;
	}

	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	public String exit(JSONObject obj) {
		System.exit(0);
		return "";
	}
	public String restart(JSONObject obj) throws Exception {
		Util.SaveJSONObjectToFile(GetRebootFileName(), obj);
		return exit(obj);	
	}
}
