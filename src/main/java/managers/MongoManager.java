package managers;

import java.util.ArrayList;

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
import managers.mongomanager.MongoManagerHelper;
import util.AssistantBotException;
import util.db.MongoUtil;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class MongoManager extends AbstractManager {
	private MongoDatabase db_;
	@SuppressWarnings("deprecation")
	public MongoManager(ResourceProvider rp) {
		super(GetCommands());
		db_ = rp.getMongoClient().getDatabase(MongoUtil.getLogistics());
	}
	private static JSONArray GetCommands() {
		return new JSONArray()
				.put(new ParseOrderedCmd("mongocv", "fix date",
						new ParseOrderedArg("collfield", ParseOrdered.ArgTypes.string)
						,new ParseOrderedArg("src", ParseOrdered.ArgTypes.string)
						,new ParseOrderedArg("dest", ParseOrdered.ArgTypes.string))
						)
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
					arr.add(arg0.getObjectId(MongoManagerHelper.IDFIELD));
				}
			}
		});
		for(ObjectId oid:arr) {
			coll.updateOne(Filters.eq(MongoManagerHelper.IDFIELD, oid), Updates.unset(split[1]));
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
	public String mongocv(JSONObject obj) throws Exception {
		final String[] split = obj.getString("collfield").split("/",2);
		if(split.length!=2)
			throw new AssistantBotException(AssistantBotException.Type.MONGOMANAGER, 
					String.format("split.length==%d!=2", split.length));
		String src = obj.getString("src").toUpperCase(),
				dest = obj.getString("dest").toUpperCase();
		int res = -1;
		if(src.equals("STRING") && dest.equals("DATE")) {
			res = MongoManagerHelper.Fixdate(split[0],split[1],db_);
		} else if(src.equals("INT") && dest.equals("DOUBLE")) {
			res = MongoManagerHelper.Fixint(split[0],split[1],db_);
		} else {
			throw new AssistantBotException(
					AssistantBotException.Type.MONGOMANAGER
					,String.format("unknown types \"%s\" and \"%s\"",src,dest));
		}
		return String.format("fixed %d", res);
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}

}
