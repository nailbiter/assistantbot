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

	protected static String PrintArgs(JSONObject cmd)
	{
		StringBuilder sb = new StringBuilder();
		int index = 0;
		JSONArray args = cmd.getJSONArray("args");
		
		if(args.length()==0)
			return sb.toString();
		
		sb.append(ParseOrdered.PrintArg(args.getJSONObject(index)));
		for(index++;index<args.length(); index++)
			sb.append(" "+ParseOrdered.PrintArg(args.getJSONObject(index)));
		
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
			res.put(cmd.getString("name"), String.format("%s%s", printArgs(cmd),cmd.optString("help","(none)")));
		}
		return res;
	}
	protected static String printArgs(JSONObject cmd)
	{
		StringBuilder sb = new StringBuilder();
		int index = 0;
		JSONArray args = cmd.getJSONArray("args");
		
		if(args.length()==0)
			return sb.toString();
		
		sb.append(printArg(args.getJSONObject(index)));
		for(index++;index<args.length(); index++)
			sb.append(" "+printArg(args.getJSONObject(index)));
		
		sb.append(": ");
		return sb.toString();
	}
	protected static String printArg(JSONObject arg)
	{
		if(IsArgOpt(arg))
			return String.format("[%s%s]", arg.getString("name").toUpperCase(),
					arg.getString("type").substring(0, 1));
		else
			return String.format("%s%s", arg.getString("name").toUpperCase(),
					arg.getString("type").substring(0, 1));
	}

	public JSONObject parse(JSONObject obj) throws Exception {
		System.err.format("parse of %s got %s", name_,obj.toString(2));

		String[] tokens = new String[0];
		if(obj.has(REM))
			tokens = obj.getString(REM).split(" ");
		JSONArray args = JsonUtil.FindInJSONArray(cmds_, "name", obj.getString(CMD)).getJSONArray("args");
		JSONObject res = new JSONObject().put("name", obj.getString(CMD));
		for(int j = 0; j < args.length();j++)
		{
			/**
			 *FIXME: use StandardParser.ArgTypes here in place of string literals
			 */
			JSONObject arg = args.getJSONObject(j);
			if(arg.getString("type").compareTo("string")==0)
			{
				if(!IsArgOpt(arg) || (tokens.length>j))
					res.put(arg.getString("name"),tokens[j]);
				continue;
			}
			//FIXME: next line is bad
			if(arg.getString("type").compareTo("int")==0 || arg.getString("type").compareTo("integer")==0)
			{
				if(!IsArgOpt(arg) || (tokens.length>j))
					res.put(arg.getString("name"),Integer.parseInt(tokens[j]));
				continue;
			}
			if(arg.getString("type").compareTo("remainder")==0)
			{
				if( IsArgOpt(arg) && !( ( j ) < tokens.length ) ) 
					continue;
				StringBuilder sb = new StringBuilder(tokens[j]);
				/*FIXME: the next snippet may cause troubles if
				 *  tokens are separated by several whitespace chars
				 *  and this needed to be preserved
				 */ 
				for(int k = j+1; k < tokens.length; k++)
					sb.append(" "+tokens[k]);
				res.put(arg.getString("name"),sb.toString());
				break;
			}
			throw new Exception("unknown type: "+arg.optString("type"));
		}
		return res;
	}
}
