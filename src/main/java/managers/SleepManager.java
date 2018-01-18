package managers;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import util.MyBasicBot;

public class SleepManager extends AbstractManager {
	protected MyBasicBot bot_ = null;
	protected boolean isSleeping;
	
	public SleepManager(MyBasicBot bot) {
		bot_ = bot;
		isSleeping = false;
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
		//TODO
		this.isSleeping = true;
		return "start sleeping";
	}
	public String sleepend(JSONObject obj)
	{
		//TODO
		this.isSleeping = false;
		return "TODO";
	}
	public boolean isSleeping()
	{
		return this.isSleeping;
	}
}
