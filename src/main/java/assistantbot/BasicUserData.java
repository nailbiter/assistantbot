package assistantbot;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import managers.AbstractManager;
import managers.MyManager;
import util.Util;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;
import util.parsers.StandardParserInterpreter;

public class BasicUserData extends AbstractManager {
	protected List<MyManager> managers_ = new ArrayList<MyManager>();
	protected StandardParserInterpreter parser_;
	protected JSONObject userObject_ = null;
	protected final boolean isSingleUser_;
	protected BasicUserData(boolean isSingleUser) throws JSONException, Exception {
		super(GetCommands(isSingleUser));
		isSingleUser_ = isSingleUser;
		if(isSingleUser)
			userObject_ = Util.GetDefaultUser();
	}
	private static JSONArray GetCommands(boolean isSingleUser) {
		JSONArray res = new JSONArray()
				.put(new ParseOrderedCmd("help", "display this help message"))
				.put(new ParseOrderedCmd("managers", "operations on managers"
						,new ParseOrderedArg("managername",ArgTypes.string)
							.makeOpt()
						,new ParseOrderedArg("command",ArgTypes.string)
							.useDefault("")))
				;
		if( !isSingleUser )
			res.put(new ParseOrderedCmd("start", "display this message"
					,new ParseOrderedArg("login",ArgTypes.string)
						.makeOpt()
					,new ParseOrderedArg("pass",ArgTypes.string)
						.useDefault("")));
		return res;
	}
	public String managers(JSONObject obj) throws Exception {
		if( !obj.has("managername") ) {
			StringBuilder sb = new StringBuilder();
			for(MyManager am:managers_) {
				sb.append(String.format("%s\n", am));
			}
			return sb.toString();
		} else {
			String pref = obj.getString("managername");
			MyManager manager = null;
			for(MyManager am:managers_) {
				if(am.toString().startsWith(pref))
					manager = am;
			}
			if( manager == null ) {
				return String.format("no manager starting with \"%s\"", pref);
			} else {
				return String.format("will call command \"%s\" on \"%s\"", 
						obj.getString("command"),manager.toString());
			}
		}
	}
	public String start(JSONObject obj) throws Exception {
		if(isSingleUser_)
			throw new Exception(String.format("cannot call \"%s\" on single-user", "start"));
		if( !obj.has("login") ) {
			return unlogin();
		} else {
			return login(obj.getString("login"),obj.getString("pass"));
		}
	}
	public String help(JSONObject obj) {
		return parser_.getHelpMessage();
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	protected String unlogin() throws JSONException, Exception {
		return null;
	}
	protected String login(String username, String password) throws JSONException, Exception {
		return null;
	}

}
