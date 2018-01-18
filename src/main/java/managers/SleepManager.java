package managers;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import util.MyBasicBot;

public class SleepManager extends AbstractManager {
	MyBasicBot bot_ = null;
	public SleepManager(MyBasicBot bot) {
		bot_ = bot;
	}

	@Override
	public JSONArray getCommands() {
		return new JSONArray()
				.put(makeCommand("sleepstart","start sleeping",new ArrayList<JSONObject>()))
				.put(makeCommand("sleepend","end sleeping",new ArrayList<JSONObject>()));
	}

	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}

}
