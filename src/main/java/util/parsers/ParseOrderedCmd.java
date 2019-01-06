package util.parsers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ParseOrderedCmd extends JSONObject {
	private static final String MULTILINEKEY = "ISMULTILINE";
	public ParseOrderedCmd(String name,String help,List<JSONObject> args) {
		put("name", name);
		put("help", (help==null)?"(null)":help);
		JSONArray array = new JSONArray();
		for(int i = 0; i < args.size(); i++)
			array.put(args.get(i));
		put("args", array);
	}
	public ParseOrderedCmd(String name,String help,JSONObject ...args) {
		put("name", name);
		put("help", (help==null)?"(null)":help);
		JSONArray array = new JSONArray();
		for(JSONObject arg:args)
			array.put(arg);
		put("args", array);
	}
	public ParseOrderedCmd(String name, String help) {
		this(name,help,new ArrayList<JSONObject>());
	}
	public ParseOrderedCmd makeDefaultHandler() {
		put(StandardParserInterpreter.DEFMESSAGEHANDLERKEY, true);
		return this;
	}
	public static boolean IsDefaultHandler(JSONObject cmd) {
		return cmd.optBoolean(StandardParserInterpreter.DEFMESSAGEHANDLERKEY, false);
	}
	public ParseOrderedCmd makeMultiline() {
		put(MULTILINEKEY,true);
		return this;
	}
	public static boolean IsMultiline(JSONObject cmd) {
		return cmd.optBoolean(MULTILINEKEY,false);
	}
	public ParseOrderedCmd addArgument(JSONObject arg) {
		getJSONArray("args").put(arg);
		return this;
	}
}
