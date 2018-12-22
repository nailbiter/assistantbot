package managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.model.Sorts;

import assistantbot.ResourceProvider;
import util.MongoUtil;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class GymManager extends AbstractManager {
	private MongoClient mongoClient_;
	private JSONObject gymSingleton_;
	private Logger logger_;
	int dayCount_ = -1;
	private JSONArray program_;
	private int exercisenum_;
	private ResourceProvider rp_;

	public GymManager(ResourceProvider rp) throws Exception {
		super(GetCommands());
		mongoClient_ = rp.getMongoClient();
		logger_ = Logger.getLogger(this.getClass().getName());
		gymSingleton_ = MongoUtil.GetJsonObjectFromDatabase(mongoClient_, "logistics.gymSingleton");
		logger_.info(String.format("gymSingleton_=%s", gymSingleton_.toString()));
		rp_ = rp;
	}
	public static JSONArray GetCommands() {
		return new JSONArray()
				.put(new ParseOrderedCmd("gymlist","list gym exercises",
						new ParseOrderedArg("dayCount",ArgTypes.integer)))
				.put(new ParseOrderedCmd("gymdone","done gym exercise",
						new ParseOrderedArg("exercisenum",ArgTypes.integer)
							.makeOpt(),
						new ParseOrderedArg("comment",ArgTypes.remainder)
							.useDefault("")
				))
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
		int exercisenum = obj.optInt("exercisenum",exercisenum_);
		if(exercisenum==0 || program_.length()<exercisenum) {
			throw new Exception(String.format("(%d<=0 || %d<%d)", exercisenum,program_.length(),exercisenum));
		} else if( exercisenum < 0 ) {
			final TableBuilder tb = new TableBuilder();
			tb.addTokens("#_","name_", "comment_");
			final ArrayList<Integer> index = new ArrayList<Integer>();
			index.add(0);
			mongoClient_.getDatabase("logistics").getCollection("gymLog")
			.find().sort(Sorts.descending("_id")).limit(-exercisenum).forEach(new Block<Document>() {
				@Override
				public void apply(Document doc) {
					JSONObject obj = new JSONObject(doc.toJson());
					index.add(0, index.get(0)+1);
					tb.addTokens(
							Integer.toString(index.get(0)),
							String.format("%d:%d:%s",
								obj.getInt("weekCount"),
								obj.getInt("dayCount"),
								obj.getJSONObject("exercise").getString("name")),
							obj.optString("comment", "")
							);
				}
			});
			return tb.toString();
		} else {
			exercisenum_ = exercisenum;
			obj.remove("name");
			obj.put("dayCount",dayCount_ );
			obj.put("weekCount", gymSingleton_.getInt("weekCount"));
			obj.remove("exercisenum");
			obj.put("exercise", program_.getJSONObject(exercisenum-1));
			obj.put("date", new Date());
			mongoClient_.getDatabase("logistics").getCollection("gymLog")
				.insertOne(Document.parse(obj.toString()));
			rp_.sendMessage(String.format("gymdone #%d", exercisenum));
			return String.format("added %s to %s",obj.toString(2) ,"logistics.gymLog");
		}
	}
}
