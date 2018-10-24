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
	int dayCount_ = -1;
	private JSONArray program_;

	public GymManager(MongoClient mongoClient) throws Exception {
		mongoClient_ = mongoClient;
		logger_ = Logger.getLogger(this.getClass().getName());
		gymSingleton_ = MongoUtil.GetJsonObjectFromDatabase(mongoClient_, "logistics.gymSingleton");
		logger_.info(String.format("gymSingleton_=%s", gymSingleton_.toString()));
	}
	@Override
	public JSONArray getCommands() {
		return new JSONArray()
				.put(MakeCommand("gymlist","list gym exercises",
						asList(MakeCommandArg("dayCount",ArgTypes.integer,false))))
				.put(MakeCommand("gymdone","done gym exercise",
						asList(
								MakeCommandArg("exercisenum",ArgTypes.integer,false),
								MakeCommandArg("comment",ArgTypes.remainder,true)
								)))
				;
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	public String gymlist(JSONObject args) throws Exception{
		if(args.getInt("dayCount")<=0 || args.getInt("dayCount")>4)
			throw new Exception(String.format("%d<=0 || %d>4", args.getInt("dayCount"),args.getInt("dayCount")));
		dayCount_ = args.getInt("dayCount");
		program_ = MongoUtil.GetJsonObjectFromDatabase(mongoClient_, "logistics.gymProgram",
				new JSONObject()
				.put("weekCount", gymSingleton_.getInt("weekCount"))
				.put("dayCount", args.getInt("dayCount"))).getJSONArray("program");
		TableBuilder tb = new TableBuilder();
		tb.addNewlineAndTokens("#","name", "reps");
		int i = 1;
		for(Object o:program_) {
			JSONObject obj = (JSONObject)o;
			tb.newRow();
			tb.addToken(i++);
			tb.addToken(obj.getString("name"));
			tb.addToken(obj.getString("reps"));
		}
		return tb.toString();
	}
	public String gymdone(JSONObject obj) throws Exception{
		obj.remove("name");
		obj.put("dayCount",dayCount_ );
		obj.put("weekCount", gymSingleton_.getInt("weekCount"));
		int exercisenum = obj.getInt("exercisenum");
		obj.remove("exercisenum");
		obj.put("exercise", program_.getJSONObject(exercisenum));
		mongoClient_.getDatabase("logistics").getCollection("gymLog").insertOne(Document.parse(obj.toString()));
		return String.format("added %s to %s",obj.toString() ,"logistics.gymLog");
	}
}
