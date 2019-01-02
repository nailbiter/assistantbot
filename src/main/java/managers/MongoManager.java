package managers;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import assistantbot.MyAssistantUserData;
import assistantbot.ResourceProvider;
import util.AssistantBotException;
import util.MongoUtil;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class MongoManager extends AbstractManager {

	private ResourceProvider rp_;
	public MongoManager(ResourceProvider rp) {
		super(GetCommands());
		rp_ = rp;
	}
	private static JSONArray GetCommands() {
		return new JSONArray()
				.put(new ParseOrderedCmd("fixdate", "fix date",
						new ParseOrderedArg("collfield", ParseOrdered.ArgTypes.string)))
				;
	}
	public String fixdate(JSONObject obj) throws Exception {
		final String[] split = obj.getString("collfield").split("/",2);
		if(split.length!=2)
			throw new AssistantBotException(AssistantBotException.Type.MONGOMANAGER, 
					String.format("split.length==%d!=2", split.length));
		
		final ArrayList<ImmutablePair<ObjectId, String>> arr = 
				new ArrayList<ImmutablePair<ObjectId,String>>();
		MongoCollection<Document> coll = 
				rp_.getMongoClient().getDatabase(MongoUtil.LOGISTICS)
				.getCollection(split[0]);
		coll.find().forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				if(arg0.containsKey(split[1]) && arg0.getString(split[1])!=null)
					arr.add(new ImmutablePair<ObjectId,String>(arg0.getObjectId("_id"), 
							arg0.getString(split[1])));
			}
		});
		for(ImmutablePair<ObjectId, String> tuple:arr) {
			Date d = MongoUtil.MongoDateStringToLocalDate(tuple.right);
			coll.updateOne(Filters.eq("_id", tuple.left), Updates.set(split[1], d));
		}
		return arr.toString();
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}

}
