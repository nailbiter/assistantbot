package assistantbot;

import static util.parsers.StandardParserInterpreter.CMD;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import managers.MyManager;
import util.Util;
import util.parsers.StandardParserInterpreter;

public class BasicUserData implements MyManager {
	protected List<MyManager> managers_ = new ArrayList<MyManager>();
	protected StandardParserInterpreter parser_;
	protected JSONObject userObject_ = null;
	protected final boolean isSingleUser_;
	protected BasicUserData(boolean isSingleUser) throws JSONException, Exception {
		isSingleUser_ = isSingleUser;
		if(isSingleUser)
			userObject_ = Util.GetDefaultUser();
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	@Override
	public JSONObject getCommands() {
		JSONObject res = new JSONObject();
		res.put("help", "display this message");
		if( !this.isSingleUser_ )
			res.put("start", "display this message");
		return res;
	}
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has(CMD))
		{
			System.err.println(this.getClass().getName()+" got comd: "+res.getString(CMD));
			if(res.getString(CMD).equals("help"))
				return parser_.getHelpMessage();
			else if( res.getString(CMD).equals("start") && !isSingleUser_ ) {
				String[] split = res.optString(StandardParserInterpreter.REM,"")
						.split(StandardParserInterpreter.SPLITPATTERN);
				if( split.length==0 || split[0].isEmpty() ) {
					return unlogin();
				} else {
					String username = split[0],
							password = (split.length>=2) ? split[1] : "";
					return login(username,password);
				}
			}
		}
		throw new Exception(String.format("for res=%s", res.toString()));
	}
	protected String unlogin() throws JSONException, Exception {
		return null;
	}
	protected String login(String username, String password) throws JSONException, Exception {
		return null;
	}

}
