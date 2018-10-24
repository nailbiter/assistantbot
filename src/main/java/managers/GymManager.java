package managers;

import org.json.JSONArray;

import com.mongodb.MongoClient;

import static java.util.Arrays.asList;
import static util.parsers.StandardParser.ArgTypes;

public class GymManager extends AbstractManager {
	private MongoClient mongoClient_;

	public GymManager(MongoClient mongoClient) {
		mongoClient_ = mongoClient;
	}

	@Override
	public JSONArray getCommands() {
		return new JSONArray()
				.put(MakeCommand("gymlist","list gym exercises",asList(MakeCommandArg("day",ArgTypes.integer,false))))
				.put(MakeCommand("gymdone","done gym exercise",asList(MakeCommandArg("day",ArgTypes.integer,false))))
				;
	}

	@Override
	public String processReply(int messageID, String msg) {
		// TODO Auto-generated method stub
		return null;
	}

}
