package util.parsers;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import util.parsers.StandardParserInterpreter.ArgTypes;

public class ParseOrdered {

	public ParseOrdered(JSONArray commands) {
		// TODO Auto-generated constructor stub
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

	public static JSONObject MakeCommandArg(String name,StandardParserInterpreter.ArgTypes type,boolean isOpt)
	{
		JSONObject arg = new JSONObject();
		
		arg.put("name", name);
		if(isOpt) arg.put("isOpt", isOpt);
		arg.put("type", type.toString());
		
		return arg;
	}

	public JSONObject getCommands() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object parse(JSONObject res) {
		// TODO Auto-generated method stub
		return null;
	}

//	String cmd = tokens[0],
//			rem = (tokens.length>1)?tokens[1]:"";
	
//	String[] tokens = line.split(" ");
//	for(int i = 0; i < cmds_.length(); i++)
//	{
//		if(cmds_.optJSONObject(i)==null)
//			continue;
//		if(tokens[0].compareTo(prefix_+cmds_.getJSONObject(i).getString("name"))==0)
//		{
//			JSONArray args = cmds_.getJSONObject(i).getJSONArray("args");
//			JSONObject res = new JSONObject().put("name", cmds_.getJSONObject(i).getString("name"));
//			for(int j = 0; j < args.length();j++)
//			{
//				/**
//				 *FIXME: use StandardParser.ArgTypes here in place of string literals
//				 */
//				JSONObject arg = args.getJSONObject(j);
//				if(arg.getString("type").compareTo("string")==0)
//				{
//					if(!isArgOpt(arg) || (tokens.length>=(j+2)))
//						res.put(arg.getString("name"),tokens[j+1]);
//					continue;
//				}
//				//FIXME: next line is bad
//				if(arg.getString("type").compareTo("int")==0 || arg.getString("type").compareTo("integer")==0)
//				{
//					if(!isArgOpt(arg) || (tokens.length>=(j+2)))
//						res.put(arg.getString("name"),Integer.parseInt(tokens[j+1]));
//					continue;
//				}
//				if(arg.getString("type").compareTo("remainder")==0)
//				{
//					if( isArgOpt(arg) && !( ( j + 1 ) < tokens.length ) ) 
//						continue;
//					StringBuilder sb = new StringBuilder(tokens[j+1]);
//					/*FIXME: the next snippet may cause troubles if
//					 *  tokens are separated by several whitespace chars
//					 *  and this needed to be preserved
//					 */ 
//					for(int k = j+2; k < tokens.length; k++)
//						sb.append(" "+tokens[k]);
//					res.put(arg.getString("name"),sb.toString());
//					break;
//				}
//				throw new Exception("unknown type: "+arg.optString("type"));
//			}
//			return res;
//		}
//	}
//	
//	return new JSONObject().put(this.defaultName_, line);
}
