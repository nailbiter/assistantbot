package util.parsers;

import org.json.JSONObject;

import util.MyManager;

public interface AbstractParser extends MyManager {

	JSONObject parse(String line) throws Exception;

	String getHelpMessage();

}
