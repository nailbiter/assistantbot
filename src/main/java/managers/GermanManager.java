package managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import util.parsers.StandardParser;

public class GermanManager extends AbstractManager {
	enum DudenConst{ GENDER, PLURAL}
	private MongoCollection<Document> genderCollection_;
	@Override
	public JSONArray getCommands() {
		return new JSONArray()
				.put(AbstractManager.MakeCommand("germangender", "german gender",
				Arrays.asList(MakeCommandArg("word",StandardParser.ArgTypes.remainder,false))))
				.put(AbstractManager.MakeCommand("germanplural", "german plural",
						Arrays.asList(MakeCommandArg("word",StandardParser.ArgTypes.remainder,false))));
	}
	public GermanManager(MongoClient mc){
		genderCollection_ = mc.getDatabase("logistics").getCollection("gender");
	}
	public String germangender(JSONObject obj) throws Exception {
		return duden(obj.getString("word"),DudenConst.GENDER);
	}
	public String germanplural(JSONObject obj) throws Exception {
		return duden(obj.getString("word"),DudenConst.PLURAL);
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
    			doc.put("plural",ExecuteCommand(String.format("duden -g Plural %s",key)).split("\n")[0].split("\\|")[1].trim());
    		}
    		catch(Exception e) {}
    		try {
    			doc.put("value",ExecuteCommand(String.format("duden --title %s", key)));
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
	static String ExecuteCommand(String command) throws IOException{
    	Runtime rt = Runtime.getRuntime();
    	Process proc = rt.exec(command);

    	BufferedReader stdInput = new BufferedReader(new 
    	     InputStreamReader(proc.getInputStream()));

    	BufferedReader stdError = new BufferedReader(new 
    	     InputStreamReader(proc.getErrorStream()));

    	// read the output from the command
    	System.out.println(String.format("Here is the standard output of the command \"%s\":\n",command));
    	String s = null;
    	StringBuilder sb = new StringBuilder();
    	while ((s = stdInput.readLine()) != null) {
    		sb.append(s+"\n");
    	}
    	return sb.toString().trim();
    }
}
