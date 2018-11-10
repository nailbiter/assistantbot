package managers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.MongoClient;

import util.MongoUtil;
import util.TableBuilder;

import static java.util.Arrays.asList;
import static util.parsers.StandardParser.ArgTypes;
import static managers.AbstractManager.MakeCommand;
import static managers.AbstractManager.MakeCommandArg;

import java.util.ArrayList;

public class ReportManager extends AbstractManager {
	private MongoClient mc_;
//	private final static JSONArray MODES = new JSONArray();
	public ReportManager(MongoClient mc) {
		mc_ = mc;
	}
	@Override
	public JSONArray getCommands() {
		return new JSONArray()
				.put(MakeCommand("reportshow", "masha reminder", asList(MakeCommandArg("type",ArgTypes.integer,true))));
	}
	public String reportshow(JSONObject obj) throws JSONException, Exception {
		if(obj.has("type")) {
			JSONObject oo = MongoUtil.GetJsonObjectFromDatabase(mc_, "logistics.reportDescriptions", new JSONObject().put("type", obj.getInt("type")));
			return oo.toString();
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
