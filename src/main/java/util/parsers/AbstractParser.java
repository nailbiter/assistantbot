package util.parsers;

import org.json.JSONArray;
import org.json.JSONObject;

import managers.MyManager;

public abstract class AbstractParser implements MyManager {

	public abstract JSONObject parse(String line) throws Exception;
	@Override
	public String processReply(int messageID,String msg) { return null; }
	abstract String getHelpMessage();
	public JSONArray getCommands() {
		return AbstractParser.getCommandsStatic();
	}
	protected static JSONArray getCommandsStatic()
	{
		return new JSONArray("[{\"name\":\"help\",\"args\":[],\"help\":\"display this message\"}]");
	}
}
