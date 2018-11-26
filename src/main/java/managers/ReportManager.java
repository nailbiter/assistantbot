package managers;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.MongoClient;

import assistantbot.ResourceProvider;
import managers.misc.MashaRemind;
import util.KeyRing;
import util.MongoUtil;
import util.ScriptApp;
import util.ScriptHelperImpl;
import util.Util;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrdered.ArgTypes;

import static java.util.Arrays.asList;

public class ReportManager extends AbstractManager {
	private MongoClient mc_;
	private TrelloAssistant ta_;
	private ResourceProvider rp_;
	private ScriptApp sa_;
	private ScriptHelperImpl sih_;
	public ReportManager(ResourceProvider rp) {
		super(GetCommands());
		mc_ = rp.getMongoClient();
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		rp_ = rp;
		sih_ = new ScriptHelperImpl(rp);
		sa_ = new ScriptApp(Util.getScriptFolder(), sih_);
	}
	public static JSONArray GetCommands() {
		return new JSONArray()
				.put(ParseOrdered.MakeCommand("reportshow", "masha reminder", asList(ParseOrdered.MakeCommandArg("type",ParseOrdered.ArgTypes.integer,true))));
	}
	public String mashareport(JSONObject obj) throws Exception {
		return MashaRemind.Remind(ta_,mc_);
	}
	public String myreport(JSONObject obj) throws Exception {
		JSONObject settings = getParamObject(mc_);
		System.err.format("got object %s\n", settings.toString(2));
		sih_.setParamObject(settings);
		String res = sa_.runCommand("timestat -e 4 -u WEEK"),
				res2 = sa_.runCommand("timestat -d money -e 4 -u WEEK");
		rp_.sendFile(Util.saveToTmpFile(
				String.format("<html>\n<head><style type=\"text/css\">\n%s\n</style></head>\n%s\n<br><br>\n%s\n</html>",
						settings.getString("cssstyle"),
						res,res2)));
		return "";
	}
	public String reportshow(JSONObject obj) throws Exception {
		if(obj.has("type")) {
			JSONObject oo = MongoUtil.GetJsonObjectFromDatabase(mc_, "logistics.reportDescriptions", new JSONObject().put("type", obj.getInt("type")));
			return (String)this.getClass().getMethod(oo.getString("callback"),JSONObject.class)
					.invoke(this,oo);
		} else {
			TableBuilder tb = new TableBuilder();
			tb.addNewlineAndTokens("type", "description");
			JSONArray reports = MongoUtil.GetJSONArrayFromDatabase(mc_, "logistics", "reportDescriptions");
			for(Object o:reports) {
				JSONObject oo = (JSONObject)o;
				tb.newRow();
				tb.addToken(oo.getInt("type"));
				tb.addToken(oo.getString("description"));
			}
			return tb.toString();
		}
	}
	
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}

}
