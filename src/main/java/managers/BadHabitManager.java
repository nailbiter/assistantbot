package managers;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import util.parsers.StandardParser;

public class BadHabitManager extends AbstractManager {
	BadHabitManager()
	{
		
	}
	@Override
	public JSONArray getCommands() {
		String pref = "bh";
		JSONArray res = new JSONArray();
		res.put(super.makeCommand(pref+"alcohol", "drank alcohol", new ArrayList<JSONObject>()));
		res.put(super.makeCommand(pref+"porn", "watched porn", new ArrayList<JSONObject>()));
		res.put(super.makeCommand(pref+"mast", "masturbated", new ArrayList<JSONObject>()));
		res.put(super.makeCommand(pref+"s", "watch bad habits", 
				Arrays.asList(super.makeCommandArg("count", StandardParser.ArgTypes.integer, true))));
		return res;
	}

	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
}
