package util;

import org.json.JSONObject;

public interface MyManager {
	abstract public String getResultAndFormat(JSONObject res) throws Exception;
}
