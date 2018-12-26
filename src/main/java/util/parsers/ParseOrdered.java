package util.parsers;

import java.util.List;

import org.apache.commons.collections4.Transformer;
import org.json.JSONArray;
import org.json.JSONObject;

import util.AssistantBotException;
import util.JsonUtil;
import static util.parsers.StandardParserInterpreter.CMD;
import static util.parsers.StandardParserInterpreter.REM;

public class ParseOrdered {

	public static enum ArgTypes{remainder, string, integer}

	private JSONArray cmds_;
	private String name_;
	private JSONObject memoTable_;

	public ParseOrdered(JSONArray commands,String name) {
		cmds_ = commands;
		name_ = name;
		memoTable_ = new JSONObject();
	}
	/**
	 * @deprecated use {@link #util.parsers.ParseOrderedCmd} instead 
	 * @param name
	 * @param help
	 * @param args
	 * @return
	 */
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
	
	/**
	 * @deprecated use {@link #util.parsers.ParseOrderedArg} instead
	 * @param name
	 * @param type
	 * @param isOpt
	 * @return
	 */
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
		
		sb.append(ParseOrderedArg.PrintArg(args.getJSONObject(index)));
		for(index++;index<args.length(); index++)
			sb.append(" "+ParseOrderedArg.PrintArg(args.getJSONObject(index)));
		
		sb.append(": ");
		return sb.toString();
	}
	public JSONObject parse(JSONObject obj) throws Exception {
		System.err.format("parse of %s got %s\n", name_,obj.toString(2));

		String line = obj.optString(REM,null);
		JSONArray args = JsonUtil.FindInJSONArray(cmds_, "name", obj.getString(CMD)).getJSONArray("args");
		JSONObject res = new JSONObject().put("name", obj.getString(CMD));
		
		int j = 0;
		for( ; j < args.length() && line != null; j++ ) {
			System.err.format("line: \"%s\"\n", line);
			String[] split = line.split(" +",2);
			System.err.format("split[0]=\"%s\"\nsplit[1]=\"%s\"\n",(split.length>=1)?split[0]:"null",(split.length>=2)?split[1]:"null");
			/**
			 *FIXME: use StandardParser.ArgTypes here in place of string literals
			 */
			JSONObject arg = args.getJSONObject(j);
			Object lastArg = null;
			if(arg.getString("type").equals("string")) {
				lastArg = split[0];
			} else if(arg.getString("type").equals("int") || arg.getString("type").equals("integer")) {
				//FIXME: previous line was bad
				lastArg = Integer.parseInt(split[0]);
			} else if(arg.getString("type").equals("remainder")) {
				lastArg = line;
			} else
				throw new Exception("unknown type: "+arg.optString("type"));
			
			System.err.format("arg %s of type %s\n", lastArg.toString(),arg.getString("type"));
			res.put(arg.getString("name"), lastArg);
			memorize(obj.getString(CMD),arg.getString("name"),lastArg);
			line = (split.length==2) ? split[1] : null;
		}
		for( ; j < args.length(); j++ ) {
			JSONObject arg = args.getJSONObject(j); 
			if( !ParseOrderedArg.IsArgOpt(arg) ) {
				throw new Exception("not enough arguments");
			} else if(ParseOrderedArg.IsUsingMemory(arg)){
				Object prevValue = getMemorized(obj.getString(CMD),arg.getString("name"),arg);
				System.err.format("getting memorized argument %s for %s.%s\n", prevValue.toString(),obj.getString(CMD),arg.getString("name"));
				res.put(arg.getString("name"), prevValue);
			} else if(ParseOrderedArg.GetMemoryTransformer(arg)!=null){
				Transformer<Object,Object> t =
						ParseOrderedArg.GetMemoryTransformer(arg);
				Object prevValue = getMemorized(obj.getString(CMD),arg.getString("name"),arg);
				System.err.format("getting memorized argument %s for %s.%s\n", prevValue.toString(),obj.getString(CMD),arg.getString("name"));
				Object newValue = t.transform(prevValue);
				System.err.format("new value: %s\n", newValue.toString());
				memorize(obj.getString(CMD),arg.getString("name"),newValue);
				res.put(arg.getString("name"), newValue);
			} else if(ParseOrderedArg.GetDefault(arg)!=null) {
				Object defValue = ParseOrderedArg.GetDefault(arg);
				System.err.format("getting default argument %s for %s.%s\n", 
						defValue.toString(),obj.getString(CMD),arg.getString("name"));
				res.put(arg.getString("name"), defValue);
			}
		}
			
		return res;
	}
	private static String GetMemoKey(String cmd, String arg) {
		return cmd+"."+arg;
	}
	private void memorize(String cmd, String arg, Object lastArg) {
		memoTable_.put(GetMemoKey(cmd,arg), lastArg);
	}
	private Object getMemorized(String cmd, String arg, JSONObject obj) throws AssistantBotException{
		String key = GetMemoKey(cmd,arg);
		if(memoTable_.has(key)) {
			return memoTable_.get(key);
		} else if(ParseOrderedArg.GetDefault(obj)!=null) {
			return ParseOrderedArg.GetDefault(obj);
		} else {
			throw new AssistantBotException(AssistantBotException.Type.NOMEMORIZEDARG, key);
		}
	}
}
