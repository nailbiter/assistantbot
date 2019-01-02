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
import com.mongodb.client.MongoDatabase;
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
	protected static final String IDFIELD = "_id";
	private MongoDatabase db_;
	public MongoManager(ResourceProvider rp) {
		super(GetCommands());
		db_ = rp.getMongoClient().getDatabase(MongoUtil.LOGISTICS);
	}
	private static JSONArray GetCommands() {
		return new JSONArray()
				.put(new ParseOrderedCmd("fixdate", "fix date",
						new ParseOrderedArg("collfield", ParseOrdered.ArgTypes.string)))
				.put(new ParseOrderedCmd("mongocp",
						"copy objects from one collection to another"
						,new ParseOrderedArg("src",ParseOrdered.ArgTypes.string)
						,new ParseOrderedArg("dest",ParseOrdered.ArgTypes.string)))
				.put(new ParseOrderedCmd("mongomv",
						"move objects from one collection to another"
						,new ParseOrderedArg("src",ParseOrdered.ArgTypes.string)
						,new ParseOrderedArg("dest",ParseOrdered.ArgTypes.string)))
				.put(new ParseOrderedCmd("mongormfield","remove field in collection"
						,new ParseOrderedArg("collfield",ParseOrdered.ArgTypes.string)))
				;
	}
	public String mongormfield(JSONObject obj) throws Exception {
		final String[] split = obj.getString("collfield").split("/",2);
		if(split.length!=2)
			throw new AssistantBotException(AssistantBotException.Type.MONGOMANAGER, 
					String.format("split.length==%d!=2", split.length));
		
		MongoCollection<Document> coll = db_.getCollection(split[0]);
		final ArrayList<ObjectId> arr = new ArrayList<ObjectId>();
		coll.find().forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				if(arg0.containsKey(split[1])) {
					arr.add(arg0.getObjectId(IDFIELD));
				}
			}
		});
		for(ObjectId oid:arr) {
			coll.updateOne(Filters.eq(IDFIELD, oid), Updates.unset(split[1]));
		}
		
		return String.format("remove field \"%s\" from coll \"%s\"", split[1],split[0]);
	}
	public String mongocp(JSONObject obj) throws Exception {
		final String[] splitsrc = obj.getString("src").split("/",2)
				,splitdest = obj.getString("dest").split("/",2);
		
		if(splitsrc.length!=2)
			throw new AssistantBotException(AssistantBotException.Type.MONGOMANAGER, 
					String.format("splitsrc.length==%d!=2", splitsrc.length));
		if(splitdest.length!=2)
			throw new AssistantBotException(AssistantBotException.Type.MONGOMANAGER, 
					String.format("splitdest.length==%d!=2", splitdest.length));
		if( !splitsrc[1].equals("*") )
			throw new AssistantBotException(AssistantBotException.Type.MONGOMANAGER, 
					String.format("splitsrc[1]==%s!=*", splitsrc[1]));
		if( !splitdest[1].equals("*") )
			throw new AssistantBotException(AssistantBotException.Type.MONGOMANAGER, 
					String.format("splitdest[1]==%s!=*", splitdest[1]));
		
		MongoCollection<Document> src = db_.getCollection(splitsrc[0]);
		final MongoCollection<Document> dest = db_.getCollection(splitdest[0]);
		final String I = "i";
		final JSONObject finalobj = new JSONObject()
				.put(I, 0);
		src.find().forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				dest.insertOne(arg0);
				finalobj.put(I, finalobj.getInt(I)+1);
			}
		});
		return String.format("copied %d objects from %s to %s"
				,finalobj.getInt(I)
				,splitsrc[0],splitdest[0]);
	}
	public String mongomv(JSONObject obj) throws Exception {
		String res = mongocp(obj);
		final String[] splitsrc = obj.getString("src").split("/",2);
		MongoCollection<Document> src = db_.getCollection(splitsrc[0]);
		src.drop();
		return res.replaceAll("copied", "moved");
	}
	public String fixdate(JSONObject obj) throws Exception {
		final String[] split = obj.getString("collfield").split("/",2);
		if(split.length!=2)
			throw new AssistantBotException(AssistantBotException.Type.MONGOMANAGER, 
					String.format("split.length==%d!=2", split.length));
		
		final ArrayList<ImmutablePair<ObjectId, String>> arr = 
				new ArrayList<ImmutablePair<ObjectId,String>>();
		MongoCollection<Document> coll = db_.getCollection(split[0]);
		coll.find().forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				if(arg0.containsKey(split[1]) && arg0.getString(split[1])!=null)
					arr.add(new ImmutablePair<ObjectId,String>(arg0.getObjectId(IDFIELD), 
							arg0.getString(split[1])));
			}
		});
		for(ImmutablePair<ObjectId, String> tuple:arr) {
			Date d = MongoUtil.MongoDateStringToLocalDate(tuple.right);
			coll.updateOne(Filters.eq(IDFIELD, tuple.left), Updates.set(split[1], d));
		}
		return arr.toString();
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}

}
