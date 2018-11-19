package managers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.MongoClient;

import assistantbot.MyAssistantUserData;
import assistantbot.ResourceProvider;
import managers.misc.MashaRemind;
import util.KeyRing;
import util.MongoUtil;

import static java.util.Arrays.asList;
import static util.parsers.StandardParser.ArgTypes;
import static managers.AbstractManager.MakeCommand;
import static managers.AbstractManager.MakeCommandArg;

import java.util.ArrayList;

public class ReportManager extends AbstractManager {
	private MongoClient mc_;
	private TrelloAssistant ta_;
	public ReportManager(ResourceProvider rp) {
		mc_ = rp.getMongoClient();
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
	}
	@Override
	public JSONArray getCommands() {
		return new JSONArray()
				.put(MakeCommand("reportshow", "masha reminder", asList(MakeCommandArg("type",ArgTypes.integer,true))));
	}
	public String mashareport(JSONObject obj) throws Exception {
		return MashaRemind.Remind(ta_,mc_);
	}
	public String myreport(JSONObject obj) {
		return "myreport";
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
