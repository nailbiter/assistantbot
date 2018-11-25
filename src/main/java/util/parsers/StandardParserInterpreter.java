package util.parsers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import managers.MyManager;
import util.JsonUtil;
import util.Util;

public class StandardParserInterpreter extends AbstractParser{
	private static final String DEFMESSAGEHANDLER = "DEFMESSAGEHANDLER";
	public static final String CMD = "cmd";
//	JSONArray cmds_;
//	String defaultName_ = null;
	private String prefix_ = "/";
	
	private List<MyManager> managers_ = null;
	HashMap<String,MyManager> dispatchTable_ = new HashMap<String,MyManager>();
	private JSONObject defSettings_;
	public enum ArgTypes{remainder, string, integer};
	public void setPrefix(String prefix) {
		prefix_ = prefix;
	}
//	protected String getNameOfDefault() throws Exception
//	{
//		for(int i = 0; i < cmds_.length(); i++)
//			if(cmds_.get(i) instanceof String)
//			{
//				System.err.println(String.format("defName=%s, idx=%d/%d", this.defaultName_,i,
//						cmds_.length()));
//				return (String)cmds_.get(i);
//			}
//		throw new Exception("getNameOfDefault");
//	}
//	protected static JSONArray getCommands(List<MyManager> managers) throws Exception
//	{
//		JSONArray cmds = StandardParserInterpreter.getCommandsStatic();
//		for(int i = 0; i < managers.size(); i++)
//		{
//			JSONArray cmds_ = managers.get(i).getCommands();
//			for(int j = 0; j < cmds_.length(); j++)
//				cmds.put(cmds_.get(j));
//		}
//		System.err.println("parser got: "+cmds.toString());
//		return cmds;
//	}
	public StandardParserInterpreter(List<MyManager> managers, JSONObject defSettings) throws Exception {
//		this(StandardParser.getCommands(managers));
		managers_ = managers;
		defSettings_ = defSettings;
	}
	@Override
	public String getHelpMessage() {
		JSONObject cmds = GetCommands(managers_,dispatchTable_);
		System.err.format("got cmds: %s\n", cmds.toString(2));
		return getTelegramHelpMessage(cmds);
	}
	protected static JSONObject GetCommands(List<MyManager> managers, HashMap<String, MyManager> dt) {
		dt.clear();
		JSONObject cmds = new JSONObject();
		for(MyManager m:managers) {
			JSONObject cmds_i = m.getCommands();
			JsonUtil.CopyIntoJson(cmds, cmds_i);
			for(Iterator<String> it = cmds_i.keys(); it.hasNext();) {
				dt.put(it.next(), m);
			}
		}
		return cmds;
	}
	protected static String getTelegramHelpMessage(JSONObject cmds)
	{
		ArrayList<String> keys = new ArrayList<String>(cmds.keySet());
		keys.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		StringBuilder res = new StringBuilder();
		res.append("\tthe following commands are known:\n");
		for(String key:keys)
			res.append(String.format("%s - %s\n", key,cmds.getString(key)));
		return res.toString();
	}
	@Override
	public JSONObject parse(String line) throws Exception
	{
		if(line.startsWith(prefix_)) {
			String[] tokens = line.split(" ",2);
			JSONObject res = new JSONObject();
			res.put(CMD, tokens[0].substring(prefix_.length()));
			if(tokens.length==2)
				res.put("rem", tokens[1]);
			return res;
		} else {
			if(defSettings_.has(DEFMESSAGEHANDLER))
				return new JSONObject()
						.put(CMD, defSettings_.getString(DEFMESSAGEHANDLER))
						.put("rem", line);
			throw new Exception(String.format("no default handler given and we got %s", line));
		}
	}
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has(CMD))
		{
			System.err.println(this.getClass().getName()+" got comd: "+prefix_+res.getString(CMD));
			if(res.getString(CMD).compareTo("help")==0)
				return getHelpMessage();
		}
		throw new Exception(String.format("for res=%s", res.toString()));
	}
	public static StandardParserInterpreter Create(List<MyManager> managers,JSONArray names) throws JSONException, Exception {
		StandardParserInterpreter parser = 
				new StandardParserInterpreter(managers,Util.GetDefSettingsObject(names));
		managers.add(parser);
		parser.getHelpMessage();
		return parser;
	}
}
