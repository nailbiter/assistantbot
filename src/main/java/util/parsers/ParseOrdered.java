package util.parsers;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import util.JsonUtil;
import util.Util;
import static util.parsers.StandardParserInterpreter.CMD;
import static util.parsers.StandardParserInterpreter.REM;

public class ParseOrdered {

	public static enum ArgTypes{remainder, string, integer}

	private JSONArray cmds_;
	private String name_;

	public ParseOrdered(JSONArray commands,String name) {
		cmds_ = commands;
		name_ = name;
	}
	protected static boolean IsArgOpt(JSONObject arg) {
		return arg.optBoolean("isOpt",false);
	}

	public static JSONObject MakeCommand(String name,String help,List<JSONObject> args)
	{
		JSONObject cmd = new JSONObject();
		cmd.put("name", name);
		cmd.put("help", (help==null)?"(null)":help);
		JSONArray array = new JSONArray();
		for(int i = 0; i < args.size(); i++)
			array.put(args.get(i));
		cmd.put("args", array);
		return cmd;
	}

	public static JSONObject MakeCommandArg(String name,ParseOrdered.ArgTypes type,boolean isOpt)
	{
		JSONObject arg = new JSONObject();
		
		arg.put("name", name);
		if(isOpt) arg.put("isOpt", isOpt);
		arg.put("type", type.toString());
		
		return arg;
	}

	public JSONObject getCommands() {
		JSONObject res = new JSONObject();
		for(Object o:cmds_) {
			JSONObject cmd = (JSONObject)o;
			String pref = "";
			if(cmd.optBoolean(StandardParserInterpreter.DEFMESSAGEHANDLERKEY, false))
				pref = StandardParserInterpreter.DEFMESSAGEHANDLERPREF;
			res.put(pref+cmd.getString("name"), String.format("%s%s", PrintArgs(cmd),cmd.optString("help","(none)")));
		}
		return res;
	}
	protected static String PrintArgs(JSONObject cmd)
	{
		StringBuilder sb = new StringBuilder();
		int index = 0;
		JSONArray args = cmd.getJSONArray("args");
		
		if(args.length()==0)
			return sb.toString();
		
		sb.append(PrintArg(args.getJSONObject(index)));
		for(index++;index<args.length(); index++)
			sb.append(" "+PrintArg(args.getJSONObject(index)));
		
		sb.append(": ");
		return sb.toString();
	}
	protected static String PrintArg(JSONObject arg)
	{
		if(IsArgOpt(arg))
			return String.format("[%s%s]", arg.getString("name").toUpperCase(),
					arg.getString("type").substring(0, 1));
		else
			return String.format("%s%s", arg.getString("name").toUpperCase(),
					arg.getString("type").substring(0, 1));
	}

	public JSONObject parse(JSONObject obj) throws Exception {
		System.err.format("parse of %s got %s\n", name_,obj.toString(2));

		String line = obj.optString(REM,null);
		JSONArray args = JsonUtil.FindInJSONArray(cmds_, "name", obj.getString(CMD)).getJSONArray("args");
		JSONObject res = new JSONObject().put("name", obj.getString(CMD));
		
		int j = 0;
		for( ; j < args.length() && line != null; j++ )
		{
			System.err.format("line: %s\n", line);
			String[] split = line.split(" ",2);
			System.err.format("split[0]=%s\nsplit[1]=%s\n",(split.length>=1)?split[0]:"null",(split.length>=2)?split[1]:"null");
			/**
			 *FIXME: use StandardParser.ArgTypes here in place of string literals
			 */
			JSONObject arg = args.getJSONObject(j);
			if(arg.getString("type").equals("string")) {
				res.put(arg.getString("name"),split[0]);
			} else if(arg.getString("type").equals("int") || arg.getString("type").equals("integer")) {
				//FIXME: previous line was bad
				res.put(arg.getString("name"),Integer.parseInt(split[0]));
			} else if(arg.getString("type").equals("remainder")) {
				res.put(arg.getString("name"),line);
			} else
				throw new Exception("unknown type: "+arg.optString("type"));
			
			line = (split.length==2) ? split[1] : null;
		}
		for( ; j < args.length(); j++ )
			if( !IsArgOpt(args.getJSONObject(j)) )
				throw new Exception("not enough arguments");
		return res;
	}
}
