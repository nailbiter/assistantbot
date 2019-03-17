package managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

import assistantbot.MyAssistantUserData;
import assistantbot.ResourceProvider;
import managers.mongomanager.MongoManagerHelper;
import util.AssistantBotException;
import util.Message;
import util.db.MongoUtil;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class MongoManager extends WithSettingsManager {
	private static final String COLLECTION = "collection";
	private static final String DISPLAYNUM = "displaynum";
	private MongoDatabase db_;
	@SuppressWarnings("deprecation")
	public MongoManager(ResourceProvider rp) throws AssistantBotException {
		super(GetCommands(),rp);
		db_ = rp.getMongoClient().getDatabase(MongoUtil.getLogistics());
		String[] collList = GetCollList(db_);
		addSettingEnum(COLLECTION,collList,collList,0);
		addSettingScalar(DISPLAYNUM,ParseOrdered.ArgTypes.integer,20);
	}
	private static String[] GetCollList(MongoDatabase db) {
		MongoCursor<String> names = db.listCollectionNames().iterator();
		ArrayList<String> res = new ArrayList<String>();
		for(String name = names.next();names.hasNext();name = names.next() )
			res.add(name);
		return res.toArray(new String[] {}); 
	}
	private static JSONArray GetCommands() throws AssistantBotException {
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
				.put(new ParseOrderedCmd("mongols","list"
						,new ParseOrderedArg("remainder",ArgTypes.remainder)))
				;
	}
	public String mongols(JSONObject obj) throws JSONException, Exception  {
		/*
		 * sort: #sort key (#sortrev key) 
		 * listsize: #size 20
		 * ... search
		 */
		ArrayList<ImmutableTriple<String,ParseOrdered.ArgTypes, Transformer<ImmutablePair<Object,Object>,Object>>> dispatch = 
				new ArrayList<ImmutableTriple<String,ParseOrdered.ArgTypes,Transformer<ImmutablePair<Object,Object>,Object>>>();
		dispatch.add(new ImmutableTriple<String,ParseOrdered.ArgTypes,Transformer<ImmutablePair<Object,Object>,Object>>(
				"sortrev"
				,ArgTypes.string
				,new Transformer<ImmutablePair<Object,Object>,Object>() {
					@Override
					public Object transform(ImmutablePair<Object, Object> input) {
						Object obj = input.left;
						if(obj instanceof MongoCollection<?>) {
							obj = ((MongoCollection) obj).find();
						}
						FindIterable<Document> find = (FindIterable<Document>) obj;
						String key = (String) input.right;
						return find.sort(Sorts.descending(key));
					}
				}));
		dispatch.add(new ImmutableTriple<String,ParseOrdered.ArgTypes,Transformer<ImmutablePair<Object,Object>,Object>>(
				"regex"
				,ArgTypes.remainder
				,new Transformer<ImmutablePair<Object,Object>,Object>() {
					@Override
					public Object transform(ImmutablePair<Object, Object> input) {
						MongoCollection<Document> coll = (MongoCollection<Document>) input.left;
						String line = (String) input.right;
						System.err.format("line: %s\n", line);
						String[] split = line.split(ParseOrdered.SPLITPAT,2);
						FindIterable<Document> find = coll.find(Filters.regex(split[0], Pattern.compile(split[1])));
						return find;
					}
				}));
		
		String message = obj.getString("remainder");
		final String TAGPREFIX = "#";
		ArrayList<ImmutablePair<Transformer<ImmutablePair<Object, Object>, Object>, Object>> processors = 
				new ArrayList<ImmutablePair<Transformer<ImmutablePair<Object,Object>,Object>,Object>>();
		while( message.startsWith(TAGPREFIX) ) {
			String[] split = message.split(ParseOrdered.SPLITPAT,2);
			String name = split[0].substring(TAGPREFIX.length());
			ImmutableTriple<String, ArgTypes, Transformer<ImmutablePair<Object,Object>,Object>> item = 
					IterableUtils.find(dispatch
					, new Predicate<ImmutableTriple<String,ParseOrdered.ArgTypes, Transformer<ImmutablePair<Object,Object>,Object>>>(){
						@Override
						public boolean evaluate(ImmutableTriple<String, ArgTypes, Transformer<ImmutablePair<Object,Object>,Object>> object) {
							return name.equals(object.left);
						}
			});
			
			Object arg = null;
			switch(item.middle) {
			case integer:{
				String[] ssplit = split[1].split(ParseOrdered.SPLITPAT,2);
				arg = Integer.parseInt(ssplit[0]);
				message = ssplit[1];
				break;
			}
			case remainder:
				arg = split[1];
				message = "";
				break;
			case string:{
				String[] ssplit = split[1].split(ParseOrdered.SPLITPAT,2);
				arg = ssplit[0];
				message = (ssplit.length>1)?ssplit[1]:"";
				break;
			}
			default:
				rp_.sendMessage(new Message(String.format("unknown type \"%s\"", item.middle)));
				break;
			}
			if( arg != null ) {
				processors.add(0,new ImmutablePair<Transformer<ImmutablePair<Object,Object>,Object>,Object>(item.right,arg));
			}
		}
		
		MongoCollection<Document> coll = db_.getCollection((String) getSetting(COLLECTION));
		Object o = coll;
		for(ImmutablePair<Transformer<ImmutablePair<Object, Object>, Object>, Object> processor:processors) {
			o = processor.left.transform(new ImmutablePair<Object,Object>(o,processor.right));
		}
		
		FindIterable<Document> res = (FindIterable<Document>) o;
		res.limit((int) getSetting(DISPLAYNUM));
		
		StringBuilder sb = new StringBuilder();
		res.forEach(new Block<Document>() {
			@Override
			public void apply(Document t) {
				// TODO Auto-generated method stub
				sb.append(String.format("%s\n", new JSONObject(t.toJson()).toString(2)));
			}
		});
		
		return sb.toString();
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
//	@Override
//	public String processReply(int messageID, String msg) {
//		return null;
//	}
}
