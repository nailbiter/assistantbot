package managers;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.MongoClient;

import util.parsers.StandardParser;

public class GermanManager extends AbstractManager {
	@Override
	public JSONArray getCommands() {
		return new JSONArray()
				.put(AbstractManager.makeCommand("germangender", "german gender",
				Arrays.asList(makeCommandArg("word",StandardParser.ArgTypes.string,false))))
				.put(AbstractManager.makeCommand("germanplural", "german plural",
						Arrays.asList(makeCommandArg("word",StandardParser.ArgTypes.string,false))));
	}
	public GermanManager(MongoClient mc){
		//TODO
	}
	public String germangender(JSONObject obj) {
		return String.format("germangender got %s", obj.getString("word"));
	}
	public String germanplural(JSONObject obj) {
		return String.format("germanplural got %s", obj.getString("word"));
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
}
