package managers;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import util.MongoUtil;
import util.TableBuilder;

import static java.util.Arrays.asList;
import static util.parsers.StandardParser.ArgTypes;

import java.util.logging.Logger;

public class GymManager extends AbstractManager {
	private MongoClient mongoClient_;
	private JSONObject gymSingleton_;
	private Logger logger_;

	public GymManager(MongoClient mongoClient) throws Exception {
		mongoClient_ = mongoClient;
		logger_ = Logger.getLogger(this.getClass().getName());
		gymSingleton_ = MongoUtil.GetJsonObjectFromDatabase(mongoClient_, "logistics.gymSingleton");
		logger_.info(String.format("gymSingleton_=%s", gymSingleton_.toString()));
	}
	protected MongoCollection<Document> getGymLog() {
		return mongoClient_.getDatabase("logistics").getCollection("gymLog");
	}
	@Override
	public JSONArray getCommands() {
		return new JSONArray()
				.put(MakeCommand("gymlist","list gym exercises",
						asList(MakeCommandArg("dayCount",ArgTypes.integer,false))))
				.put(MakeCommand("gymdone","done gym exercise",
						asList(MakeCommandArg("exercisenum",ArgTypes.integer,false))))
				;
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	public String gymlist(JSONObject args) throws Exception{
//		return String.format("stub for %s: got %s", "gymlist",obj.toString());
		JSONArray program = MongoUtil.GetJsonObjectFromDatabase(mongoClient_, "logistics.gymProgram",
				new JSONObject()
				.put("weekCount", gymSingleton_.getInt("weekCount"))
				.put("dayCount", args.getInt("dayCount"))).getJSONArray("program");
		TableBuilder tb = new TableBuilder();
		tb.addNewlineAndTokens("name", "reps");
		for(Object o:program) {
			JSONObject obj = (JSONObject)o;
			tb.newRow();
			tb.addToken(obj.getString("name"));
			tb.addToken(obj.getString("reps"));
		}
		return tb.toString();
	}
	public String gymdone(JSONObject obj) throws Exception{
		return String.format("stub for %s: got %s", "gymdone",obj.toString());
	}
}
