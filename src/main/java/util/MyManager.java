package util;

import org.json.JSONArray;
import org.json.JSONObject;

public interface MyManager {
	abstract public String getResultAndFormat(JSONObject res) throws Exception;
	public String gotUpdate(String data)throws Exception;
	public JSONArray getCommands();
	public String processReply(int messageID,String msg);
}
