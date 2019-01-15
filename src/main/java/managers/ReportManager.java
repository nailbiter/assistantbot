package managers;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import assistantbot.ResourceProvider;
import managers.misc.MashaRemind;
import util.JsonUtil;
import util.KeyRing;
import util.UserCollection;
import util.Util;
import util.db.MongoUtil;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;
import util.scriptapps.JsApp;
import util.scriptapps.ScriptApp;
import util.scripthelpers.ScriptHelperImpl;

import static java.util.Arrays.asList;

/**
 *@ deprecated migrate money report to MoneyManager, masha report to TrelloManager
 * @author oleksiileontiev
 *
 */
public class ReportManager extends AbstractManager {
	private static final String JSFOLDERNAME = "forreport/";
	private ResourceProvider rp_;
	private ScriptApp sa_;
	private ScriptHelperImpl sih_;
	public ReportManager(ResourceProvider rp) {
		super(GetCommands());
		rp_ = rp;
		sih_ = new ScriptHelperImpl(rp);
		sa_ = new JsApp(Util.getScriptFolder()+JSFOLDERNAME, sih_);
	}
	public static JSONArray GetCommands() {
		return new JSONArray()
				.put(new ParseOrderedCmd("reportshow", "show report"
						, new ParseOrderedArg("type",ParseOrdered.ArgTypes.integer).makeOpt()));
	}
	public String myreport(JSONObject obj) throws Exception {
		JSONObject settings = getParamObject(rp_);
		System.err.format("got object %s\n", settings.toString(2));
		sih_.setParamObject(settings);
		String res = sa_.runCommand(String.format("timestat -e %d -u %s", settings.getInt("timecount"),settings.getString("timeunit"))),
				res2 = sa_.runCommand(String.format("timestat -d money -e %s -u %s", settings.getInt("moneycount"),settings.getString("moneyunit")));
		rp_.sendFile(Util.saveToTmpFile(
				String.format("<html>\n<head><style type=\"text/css\">\n%s\n</style></head>\n%s\n<br><br>\n%s\n</html>",
						JsonUtil.JoinJsonArray(settings.getJSONArray("cssstyle"), "\n"),
						res,res2)));
		return "";
	}
	public String reportshow(JSONObject obj) throws Exception {
		if(obj.has("type")) {
			MongoCollection<Document> coll = 
					rp_.getCollection(UserCollection.REPORTDESCRIPTIONS);
			JSONObject oo = MongoUtil
					.GetJsonObjectFromDatabase(coll, "type", obj.getInt("type"));
			return (String)this.getClass().getMethod(oo.getString("callback"),JSONObject.class)
					.invoke(this,oo);
		} else {
			TableBuilder tb = new TableBuilder();
			tb.addNewlineAndTokens("type", "description");
			MongoCollection<Document> coll = 
					rp_.getCollection(UserCollection.REPORTDESCRIPTIONS);
			JSONArray reports = MongoUtil.GetJSONArrayFromDatabase(coll);
			for(Object o:reports) {
				JSONObject oo = (JSONObject)o;
				tb.newRow();
				tb.addToken(oo.getInt("type"));
				tb.addToken(oo.getString("description"));
			}
			return tb.toString();
		}
	}
}
