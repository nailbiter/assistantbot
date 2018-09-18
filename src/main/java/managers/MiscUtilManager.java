package managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.KeyRing;
import util.TrelloAssistant;
import util.parsers.StandardParser;

public class MiscUtilManager extends AbstractManager {
	Random rand_ = new Random();
	private final static String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
	private final static String UPPERCASE = LOWERCASE.toUpperCase();
	private Hashtable<String,Object> hash_ = new Hashtable<String,Object>();
	TrelloAssistant ta_;
	String tasklist_;
	private static final String TASKLISTNAME = "todo";
	
	public MiscUtilManager() throws Exception {
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		tasklist_ = ta_.findListByName(HabitManager.HABITBOARDID, TASKLISTNAME);
	}
	@Override
	public JSONArray getCommands() {
		return new JSONArray()
				.put(AbstractManager.makeCommand("rand", "return random",
				Arrays.asList(makeCommandArg("key",StandardParser.ArgTypes.string,true))))
				.put(AbstractManager.makeCommand("exit", "exit the bot", new ArrayList<JSONObject>()))
				.put(AbstractManager.makeCommand("ttask", "make new task", Arrays.asList((
						makeCommandArg("task",StandardParser.ArgTypes.remainder,false)))));
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	public String exit(JSONObject obj) {
		System.exit(0);
		return "";
	}
	public String rand(JSONObject obj){
		String key = obj.optString("key",(String)hash_.get("key"));
		hash_.put("key", key);
		int len = Integer.parseInt(key);
		StringBuilder sb = new StringBuilder();
		String alphabet = UPPERCASE+LOWERCASE;
		
		for(int i = 0; i < len; i++)
			sb.append(alphabet.charAt(rand_.nextInt(alphabet.length())));
		
		return sb.toString();
	}
	public String ttask(JSONObject obj) throws ClientProtocolException, JSONException, IOException {
		String task = obj.getString("task");
		ta_.addCard(tasklist_, new JSONObject().put("name", task));
		return String.format("task \"%s\" added", task);
	}
}
