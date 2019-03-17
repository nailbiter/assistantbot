package managers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import util.Message;
import util.scriptapps.PerlApp;

public class BotManager extends AbstractManager {
	private static final String CMDPREFIX = "";
	private static PerlApp pa_;
	private ResourceProvider rp_;

	public BotManager(ResourceProvider rp) throws JSONException, Exception {
		super((pa_ = new PerlApp(GetParamObj(rp).getString("SCRIPTDIR"),
				GetParamObj(rp).getString("INTERPRETER")))
				.getCommandsObj(CMDPREFIX));
		rp_ = rp;
	}
//	@Override
//	public Message processReply(int messageID, String msg) {
//		return null;
//	}
	protected static JSONObject GetParamObj(ResourceProvider rp) throws JSONException, Exception {
		return AbstractManager.GetParamObject(rp, BotManager.class.getName());
	}
	@Override
	public Message getResultAndFormat(JSONObject res) throws Exception {
		System.err.println(String.format("%s got: %s",this.getClass().getName(), res.toString()));
		res = (JSONObject) po_.parse(res);
		System.err.println("dispatcher got: "+res.toString());
		return new Message(pa_.runCommand(String.format("%s %s"
						,res.getString("name").substring(CMDPREFIX.length())
						,res.getString("cmdline")
						)));
	}
}
