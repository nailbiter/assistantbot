package managers;

import org.json.JSONException;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import util.Message;
import util.scriptapps.PerlApp;

public class PerlScriptManager extends AbstractManager {
	private static final String CMDPREFIX = "pl";
	private static PerlApp pa_;
	private ResourceProvider rp_;
	public PerlScriptManager(ResourceProvider rp) throws Exception {
		super((pa_ = new PerlApp(GetParamObj(rp).getString("SCRIPTDIR"),
				GetParamObj(rp).getString("INTERPRETER")))
				.getCommandsObj(CMDPREFIX));
		rp_ = rp;
	}

//	@Override
//	public String processReply(int messageID, String msg) {
//		return null;
//	}
	protected static JSONObject GetParamObj(ResourceProvider rp) throws JSONException, Exception {
		return AbstractManager.GetParamObject(rp, PerlScriptManager.class.getName());
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
