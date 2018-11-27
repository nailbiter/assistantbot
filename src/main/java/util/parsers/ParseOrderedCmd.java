package util.parsers;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ParseOrderedCmd extends JSONObject {
	public ParseOrderedCmd(String name,String help,List<JSONObject> args) {
		put("name", name);
		put("help", (help==null)?"(null)":help);
		JSONArray array = new JSONArray();
		for(int i = 0; i < args.size(); i++)
			array.put(args.get(i));
		put("args", array);
	}
	public ParseOrderedCmd makeDefaultHandler() {
		put(StandardParserInterpreter.DEFMESSAGEHANDLERKEY, true);
		return this;
	}
	public static boolean IsDefaultHandler(JSONObject cmd) {
		return cmd.optBoolean(StandardParserInterpreter.DEFMESSAGEHANDLERKEY, false);
	}
}
