package managers;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import util.LocalUtil;
import util.MyBasicBot;

public class SleepManager extends AbstractManager {
	protected MyBasicBot bot_ = null;
	protected boolean isSleeping;
	JSONObject obj_ = null;
	JSONArray sleepingtimes_ = null, wakingtimes_ = null;
	
	public SleepManager(MyBasicBot bot) {
		bot_ = bot;
		isSleeping = false;
		obj_ = util.StorageManager.get("sleep", true);
		if(!obj_.has("sleepingtimes"))
			obj_.put("sleepingtimes", new JSONArray());
		if(!obj_.has("wakingtimes"))
			obj_.put("wakingtimes", new JSONArray());
		this.sleepingtimes_ = obj_.getJSONArray("sleepingtimes");
		this.wakingtimes_ = obj_.getJSONArray("wakingtimes");
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
	public String sleepstart(JSONObject obj)
	{
		this.isSleeping = true;
		this.sleepingtimes_.put((new Date()).getTime());
		return "start sleeping";
	}
	public String sleepend(JSONObject obj)
	{
		this.isSleeping = false;
		this.wakingtimes_.put((new Date()).getTime());
		return String.format("you have slept for: %s", LocalUtil.milisToTimeFormat(
				this.wakingtimes_.getLong(this.wakingtimes_.length() - 1) - 
				this.sleepingtimes_.getLong(this.sleepingtimes_.length() - 1)));
	}
	public boolean isSleeping()
	{
		return this.isSleeping;
	}
}
