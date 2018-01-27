package managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import util.LocalUtil;
import util.StorageManager;
import util.parsers.StandardParser;

public class BadHabitManager extends AbstractManager {
	JSONArray badhabits = null;
	String[] bh = {"alcohol","porn","mast"};
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		System.out.println(String.format("got: %s", res.toString()));
		if(res.has("name") && hasCommand(res))
		{
			if(res.getString("name").equals("bhs"))
				return listBadHabits(res.optInt("count",10));
			JSONArray ress = new JSONArray()
					.put(LocalUtil.DateToString(new Date()))
					.put(res.getString("name"));
			badhabits.put(ress);
			return ress.toString();
		}
		return null;
	}
	private String listBadHabits(int count) {
		int index = this.badhabits.length() - 1;
		util.TableBuilder tb = new util.TableBuilder();
		tb.addNewlineAndTokens("what", "date");
		while(index>=0 && count>0)
		{
			tb.addNewlineAndTokens(badhabits.getJSONArray(index).getString(1),
					badhabits.getJSONArray(index).getString(0));
			count--; index--;
		}
		return tb.toString();
	}
	public BadHabitManager()
	{
		JSONObject obj = StorageManager.get("badhabits", true);
		if(!obj.has("a"))
			obj.put("a", new JSONArray());
		badhabits = obj.getJSONArray("a");
	}
	@Override
	public JSONArray getCommands() {
		String pref = "bh";
		JSONArray res = new JSONArray();
		for(int i = 0; i < bh.length; i++)
			res.put(super.makeCommand(pref+bh[i], String.format("bad habit: %s", bh[i]), 
					new ArrayList<JSONObject>()));
		res.put(super.makeCommand(pref+"s", "watch bad habits", 
				Arrays.asList(super.makeCommandArg("count", StandardParser.ArgTypes.integer, true))));
		return res;
	}

	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
}
