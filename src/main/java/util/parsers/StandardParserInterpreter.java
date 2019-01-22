package util.parsers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import managers.MyManager;
import util.AssistantBotException;
import util.JsonUtil;
import util.Util;
import static util.Util.PopulateManagers;

public class StandardParserInterpreter implements AbstractParser{
	public static final String CMD = "cmd";
	public static final String REM = "rem";
	public static final String DEFMESSAGEHANDLERKEY =  "DEFMESSAGEHANDLER";
	public static final String DEFMESSAGEHANDLERPREF =  "_";
	public static final String DEFPHOTOHANDLERKEY =  "DEFPHOTOHANDLER";
	public static final String DEFPHOTOHANDLERPREF =  "*";
	public static final String SPLITPATTERN = " +";
	private List<MyManager> managers_ = null;
	private HashMap<String,MyManager> dispatchTable_ = 
			new HashMap<String,MyManager>();
	private JSONObject defHandlers_ = new JSONObject();
	private Logger logger_;
	
	protected StandardParserInterpreter(List<MyManager> managers, JSONObject defSettings) throws Exception {
		managers_ = managers;
		logger_ = Logger.getLogger(this.getClass().getName());
	}
	@Override
	public String getHelpMessage() {
		JSONObject cmds = GetCommands(managers_,getDispatchTable(),defHandlers_);
		System.err.format("got cmds: %s\n", cmds.toString(2));
		return getTelegramHelpMessage(cmds,defHandlers_);
	}
	private static void SetDefaultHandlers(JSONObject dh, JSONObject cmds) {
		ArrayList<String> keys = new ArrayList<String>(cmds.keySet());
		for(String cmdname:keys) {
			if(cmdname.startsWith(DEFMESSAGEHANDLERPREF)) {
				cmds.put(cmdname.substring(DEFMESSAGEHANDLERPREF.length()), cmds.getString(cmdname));
				cmds.remove(cmdname);
				dh.put(DEFMESSAGEHANDLERKEY, cmdname.substring(DEFMESSAGEHANDLERPREF.length()));
			}
			if(cmdname.startsWith(DEFPHOTOHANDLERPREF)) {
				cmds.put(cmdname.substring(DEFPHOTOHANDLERPREF.length()), cmds.getString(cmdname));
				cmds.remove(cmdname);
				dh.put(DEFPHOTOHANDLERKEY, 
						cmdname.substring(DEFPHOTOHANDLERPREF.length()));
			}
		}
	}
	protected static JSONObject GetCommands(List<MyManager> managers, HashMap<String, MyManager> dt, JSONObject dh) {
		dt.clear();
		JSONObject cmds = new JSONObject();
		for(MyManager m:managers) {
			JSONObject cmds_i = m.getCommands();
			SetDefaultHandlers(dh,cmds_i);
			JsonUtil.CopyIntoJson(cmds, cmds_i);
			for(Iterator<String> it = cmds_i.keys(); it.hasNext();) {
				dt.put(it.next(), m);
			}
		}
		return cmds;
	}
	protected static String getTelegramHelpMessage(JSONObject cmds, JSONObject defHandlers)
	{
		ArrayList<String> keys = new ArrayList<String>(cmds.keySet());
		keys.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		StringBuilder res = new StringBuilder();
//		res.append("\tthe following commands are known:\n");
		for(String key:keys) {
			if( defHandlers.has(DEFPHOTOHANDLERKEY) && key.startsWith(defHandlers.getString(DEFPHOTOHANDLERKEY)) )
				continue;
			res.append(String.format("%s - %s\n", key,cmds.getString(key)));
		}
			
		return res.toString();
	}
	@Override
	public JSONObject parse(String line) throws Exception
	{
		String prefix = Util.getParsePrefix();
		logger_.info(String.format("line=\"%s\"\nprefix=\"%s\"\n", line,prefix));
		JSONObject res = null;
		if(line.startsWith(prefix)) {
			String[] tokens = line.split(SPLITPATTERN,2);
			System.err.format("split[0]=\"%s\"\nsplit[1]=\"%s\"\n",
					(tokens.length>=1)?tokens[0]:"null",(tokens.length>=2)?tokens[1]:"null");
			String cmd = tokens[0].substring(prefix.length());
			if(!getDispatchTable().containsKey(cmd))
				return defaultHandle(line.substring(prefix.length()));
			res = new JSONObject();
			res.put(CMD, cmd);
			if(tokens.length==2 && !tokens[1].isEmpty())
				res.put(REM, tokens[1]);
			return res;
		} else {
			if(defHandlers_.has(DEFMESSAGEHANDLERKEY))
				res = defaultHandle(line);
			else
				throw new Exception(String.format("no default handler given and we got %s", line));
		}
		logger_.info(String.format("returning %s\n", res.toString(2)));
		return res;
	}
	private JSONObject defaultHandle(String line) {
		return new JSONObject()
				.put(CMD, defHandlers_.getString(DEFMESSAGEHANDLERKEY))
				.put(REM, line);
	}
	public static StandardParserInterpreter Create(List<MyManager> managers,JSONArray names,ResourceProvider rp) throws JSONException, Exception {
		if(names==null) {
			names = new JSONArray();
		}
			
		PopulateManagers(managers, names, rp);
		StandardParserInterpreter parser = 
				new StandardParserInterpreter(managers,
						Util.GetDefSettingsObject(names));
		parser.getHelpMessage();
		return parser;
	}
	public HashMap<String,MyManager> getDispatchTable() {
		return dispatchTable_;
	}
	public JSONObject processImage(String fn) throws AssistantBotException {
		if( !defHandlers_.has(DEFPHOTOHANDLERKEY) )
			throw new AssistantBotException(AssistantBotException.Type.STANDARDPARSER,String.format("no default image handler!"));
		return new JSONObject()
				.put(CMD, defHandlers_.getString(DEFPHOTOHANDLERKEY))
				.put(REM, fn);
	}
}
