package managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import managers.misc.MashaRemind;
import managers.misc.RandomSetGenerator;
import util.KeyRing;
import util.Util;
import static java.util.Arrays.asList;
import static util.parsers.StandardParser.ArgTypes;
import static util.Util.GetRebootFileName;

public class MiscUtilManager extends AbstractManager {
	Random rand_ = new Random();
	private final static String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
	private final static String UPPERCASE = LOWERCASE.toUpperCase();
	private Hashtable<String,Object> hash_ = new Hashtable<String,Object>();
	TrelloAssistant ta_;
	String tasklist_ = null;
	private MongoClient mc_;
	private static final String TASKLISTNAME = "todo";
	private static final String DISTRICOLLECTIONBNAME = "randsetdistrib";
	
	
	public MiscUtilManager(MongoClient mc) throws Exception {
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		try{
			tasklist_ = ta_.findListByName(HabitManager.HABITBOARDID, TASKLISTNAME);
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
		}
		mc_ = mc;
	}
	@Override
	public JSONArray getCommands() {
		return new JSONArray()
				.put(MakeCommand("rand", "return random",
						asList(
								MakeCommandArg("key",ArgTypes.integer,true),
								MakeCommandArg("charset",ArgTypes.string,true)
								)))
				.put(MakeCommand("randset","return randomly generated set",
						asList(MakeCommandArg("size",ArgTypes.integer,false))))
				.put(MakeCommand("exit", "exit the bot", new ArrayList<JSONObject>()))
				.put(MakeCommand("masharemind", "masha reminder", new ArrayList<JSONObject>()))
				.put(MakeCommand("restart", "restart the bot",
						asList(MakeCommandArg("command",ArgTypes.remainder,true))))
				.put(MakeCommand("ttask", "make new task", Arrays.asList((
						MakeCommandArg("task",ArgTypes.remainder,false)))));
	}
	public String masharemind(JSONObject obj) throws Exception {
		return MashaRemind.Remind(ta_,mc_);
	}
	public String restart(JSONObject obj) throws Exception {
		if(obj.getString("command").equals("help")) {
			return Util.GetFile(Util.GetRebootCommandFileName());
		}
		Util.SaveJSONObjectToFile(GetRebootFileName(), obj);
		return exit(obj);
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	public String randset(JSONObject obj) {
		MongoCollection<Document> col = mc_.getDatabase("logistics").getCollection(DISTRICOLLECTIONBNAME);
		final ArrayList<JSONObject> data = new ArrayList<JSONObject>();
		col.find().forEach(new Block<Document>(){
			@Override
			public void apply(Document doc) {
				data.add(new JSONObject(doc.toJson()));
			}
		});
		
		ArrayList<String> res = RandomSetGenerator.MakeRandomSet(data,obj.getInt("size"));
		return String.format("%s", res.toString());
	}
	
	public String exit(JSONObject obj) {
		System.exit(0);
		return "";
	}
	public String rand(JSONObject obj){
		int len;
		if(obj.has("key"))
			len = obj.getInt("key");
		else
			len = (int)hash_.get("key");
		hash_.put("key", len);
		StringBuilder sb = new StringBuilder();
		String alphabet = UPPERCASE+LOWERCASE;
		
		for(int i = 0; i < len; i++)
			sb.append(alphabet.charAt(rand_.nextInt(alphabet.length())));
		
		return sb.toString();
	}
	public String ttask(JSONObject obj) throws Exception {
		String task = obj.getString("task");
		ta_.addCard(tasklist_, new JSONObject().put("name", task));
		return String.format("task \"%s\" added", task);
	}
}
