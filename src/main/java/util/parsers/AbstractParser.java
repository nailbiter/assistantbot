package util.parsers;

import org.json.JSONObject;

import managers.MyManager;

public interface AbstractParser{
	public abstract JSONObject parse(String line) throws Exception;
	public abstract String getHelpMessage();
}
