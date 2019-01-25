package util.parsers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import util.AssistantBotException;
import util.parsers.StandardParserInterpreter.DefaultHandlers;

public class ParseOrderedCmd extends JSONObject {
	private static final String MULTILINEKEY = "ISMULTILINE";
	public ParseOrderedCmd(String name,String help,List<JSONObject> args) throws AssistantBotException {
		putName(name);
		put("help", (help==null)?"(null)":help);
		JSONArray array = new JSONArray();
		for(int i = 0; i < args.size(); i++)
			array.put(args.get(i));
		put("args", array);
	}
	private void putName(String name) throws AssistantBotException {
		for(DefaultHandlers dh: StandardParserInterpreter.DefaultHandlers.values()) {
			String s = dh.getPref();
			if(name.startsWith(s))
				throw new AssistantBotException(AssistantBotException.Type.PARSEORDEREDCMD
						,String.format("command cannot start with \"%s\"", s));
		}
			
		put("name",name);
	}
	public ParseOrderedCmd(String name,String help,JSONObject ...args) throws AssistantBotException {
//		put("name", name);
		putName(name);
		put("help", (help==null)?"(null)":help);
		JSONArray array = new JSONArray();
		for(JSONObject arg:args)
			array.put(arg);
		put("args", array);
	}
	public ParseOrderedCmd(String name, String help) throws AssistantBotException {
		this(name,help,new ArrayList<JSONObject>());
	}
	public ParseOrderedCmd makeDefaultHandler() {
		put(StandardParserInterpreter.DefaultHandlers.MESSAGE.getKey(), true);
		return this;
	}
	public static boolean IsDefaultHandler(JSONObject cmd) {
		return cmd.optBoolean(StandardParserInterpreter.DefaultHandlers.MESSAGE.getKey(), false);
	}
	public ParseOrderedCmd makeDefaultPhotoHandler() {
		put(StandardParserInterpreter.DefaultHandlers.PHOTO.getKey(), true);
		return this;
	}
	public static boolean IsDefaultPhotoHandler(JSONObject cmd) {
		return cmd.optBoolean(StandardParserInterpreter.DefaultHandlers.PHOTO.getKey(), false);
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
