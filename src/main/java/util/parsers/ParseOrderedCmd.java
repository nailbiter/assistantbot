package util.parsers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import util.AssistantBotException;

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
		final Set<String> unallowed = new HashSet<String>();
		unallowed.add(StandardParserInterpreter.DEFMESSAGEHANDLERPREF);
		unallowed.add(StandardParserInterpreter.DEFPHOTOHANDLERPREF);
		
		for(String s:unallowed)
			if(name.startsWith(s))
				throw new AssistantBotException(AssistantBotException.Type.PARSEORDEREDCMD
						,String.format("command cannot start with \"%s\"", s));
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
		put(StandardParserInterpreter.DEFMESSAGEHANDLERKEY, true);
		return this;
	}
	public static boolean IsDefaultHandler(JSONObject cmd) {
		return cmd.optBoolean(StandardParserInterpreter.DEFMESSAGEHANDLERKEY, false);
	}
	public ParseOrderedCmd makeDefaultPhotoHandler() {
		put(StandardParserInterpreter.DEFPHOTOHANDLERKEY, true);
		return this;
	}
	public static boolean IsDefaultPhotoHandler(JSONObject cmd) {
		return cmd.optBoolean(StandardParserInterpreter.DEFPHOTOHANDLERKEY, false);
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
