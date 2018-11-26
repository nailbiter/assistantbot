package util.parsers;

import org.json.JSONObject;

import managers.MyManager;

public abstract class AbstractParser implements MyManager {
	public abstract JSONObject parse(String line) throws Exception;
	public abstract String getHelpMessage();
	@Override
	public String processReply(int messageID,String msg) { return null; }
	@Override
	public JSONObject getCommands() {
		return new JSONObject()
				.put("help", "display this message");
	}
}
