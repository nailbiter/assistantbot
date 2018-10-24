package managers;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.MongoClient;

import util.MongoUtil;

import static java.util.Arrays.asList;
import static util.parsers.StandardParser.ArgTypes;

import java.util.logging.Logger;

public class GymManager extends AbstractManager {
	private MongoClient mongoClient_;
	private JSONObject gymSingleton_;
	private Logger logger_;

	public GymManager(MongoClient mongoClient) throws Exception {
		mongoClient_ = mongoClient;
		logger_ = Logger.getLogger(this.getClass().getName());
		gymSingleton_ = MongoUtil.GetJsonObjectFromDatabase(mongoClient_, "logistics.gymSingleton");
		logger_.info(String.format("gymSingleton_=%s", gymSingleton_.toString()));
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
