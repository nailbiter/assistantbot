package managers;

import java.util.Arrays;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import assistantbot.ResourceProvider;
import util.Util;
import util.parsers.ParseOrdered;

public class GermanManager extends AbstractManager {
	enum DudenConst{ GENDER, PLURAL}
	private MongoCollection<Document> genderCollection_;
	public static JSONArray GetCommands() {
		return new JSONArray()
				.put(ParseOrdered.MakeCommand("germangender", "german gender",
				Arrays.asList(ParseOrdered.MakeCommandArg("word",ParseOrdered.ArgTypes.remainder,false))))
				.put(ParseOrdered.MakeCommand("germanplural", "german plural",
						Arrays.asList(ParseOrdered.MakeCommandArg("word",ParseOrdered.ArgTypes.remainder,false))));
	}
	public GermanManager(ResourceProvider rp){
		super(GetCommands());
		genderCollection_ = rp.getMongoClient().getDatabase("logistics").getCollection("gender");
	}
	protected static String EmptyWrap(String repl) {
		if(repl==null || repl.isEmpty())
			return "reply was empty";
		else
			return repl;
	}
	public String germangender(JSONObject obj) throws Exception {
		return EmptyWrap(duden(obj.getString("word"),DudenConst.GENDER));
	}
	public String germanplural(JSONObject obj) throws Exception {
		return EmptyWrap(duden(obj.getString("word"),DudenConst.PLURAL));
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	private String duden(String token, DudenConst command) throws Exception {
    	String key = token.trim();
    	Document doc = null;
    	if(genderCollection_.count(new Document("key",key))==0) {
    		doc = new Document("key", key);
    		doc.put("usageCount", 1);
    		try {
    			doc.put("plural",Util.ExecuteCommand(String.format("duden -g Plural %s",key)).split("\n")[0].split("\\|")[1].trim());
    		}
    		catch(Exception e) {}
    		try {
    			doc.put("value",Util.ExecuteCommand(String.format("duden --title %s", key)));
    		}
    		catch(Exception e) {}
    		genderCollection_.insertOne(doc);
    	}else {
    		genderCollection_.updateOne(Filters.eq("key",key),Updates.inc("usageCount", 1));
    		doc = genderCollection_.find(new Document("key",key)).first();
    	}
    	if(command==DudenConst.GENDER)
    		return (String) doc.get("value");
    	else if(command==DudenConst.PLURAL)
    		return (String) doc.get("plural");
    	else
    		throw new Exception("command not found");
	}
}
