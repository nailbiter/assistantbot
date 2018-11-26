package util.parsers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import managers.MyManager;
import util.JsonUtil;
import util.Util;
import static util.Util.PopulateManagers;

public class StandardParserInterpreter extends AbstractParser{
	public static final String DEFMESSAGEHANDLER = "DEFMESSAGEHANDLER";
	public static final String CMD = "cmd";
	public static final String REM = "rem";
	private List<MyManager> managers_ = null;
	private HashMap<String,MyManager> dispatchTable_ = new HashMap<String,MyManager>();
	private JSONObject defSettings_;
	
	protected StandardParserInterpreter(List<MyManager> managers, JSONObject defSettings) throws Exception {
		managers_ = managers;
		defSettings_ = defSettings;
	}
	@Override
	public String getHelpMessage() {
		JSONObject cmds = GetCommands(managers_,getDispatchTable());
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
		String prefix = Util.getParsePrefix();
		if(line.startsWith(prefix)) {
			String[] tokens = line.split(" ",2);
			JSONObject res = new JSONObject();
			res.put(CMD, tokens[0].substring(prefix.length()));
			if(tokens.length==2)
				res.put(REM, tokens[1]);
			return res;
		} else {
			if(defSettings_.has(DEFMESSAGEHANDLER))
				return new JSONObject()
						.put(CMD, defSettings_.getString(DEFMESSAGEHANDLER))
						.put(REM, line);
			throw new Exception(String.format("no default handler given and we got %s", line));
		}
	}
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has(CMD))
		{
			System.err.println(this.getClass().getName()+" got comd: "+res.getString(CMD));
			if(res.getString(CMD).compareTo("help")==0)
				return getHelpMessage();
		}
		throw new Exception(String.format("for res=%s", res.toString()));
	}
	public static StandardParserInterpreter Create(List<MyManager> managers,JSONArray names,ResourceProvider rp) throws JSONException, Exception {
		PopulateManagers(managers, names, rp);
		StandardParserInterpreter parser = 
				new StandardParserInterpreter(managers,Util.GetDefSettingsObject(names));
		managers.add(parser);
		parser.getHelpMessage();
		return parser;
	}
	/**
	 * @return the dispatchTable_
	 */
	public HashMap<String,MyManager> getDispatchTable() {
		return dispatchTable_;
	}
}
