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
		return MashaRemind.Remind(ta_);
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
		final ArrayList<JSONObject> data = new ArrayList<JSONObject>(),
				distribution = new ArrayList<JSONObject>();
		col.find().forEach(new Block<Document>(){
			@Override
			public void apply(Document doc) {
				data.add(new JSONObject(doc.toJson()));
			}
		});
		
		ArrayList<String> res = new ArrayList<String>();
		while(res.size()<obj.getInt("size")) {
			System.out.format("iteration #%d\n", res.size()+1);
			GenerateDistribution(data,distribution);
			System.out.format("dist: %s\n", distribution.toString());
			String resS = PickElement(data,distribution);
			System.out.format("resS: %s\n", resS);
			res.add(resS);
			RemoveElementByKey(data,resS);
		}
		
		Collections.sort(res);
		return String.format("%s", res.toString());
	}
	private void RemoveElementByKey(ArrayList<JSONObject> data, String resS) {
		for(int i = 0; i < data.size(); i++) {
			if(data.get(i).getString("key").equals(resS))
			{
				data.remove(i);
				return;
			}
		}
	}
	private String PickElement(ArrayList<JSONObject> data, ArrayList<JSONObject> distribution) {
		double pick = rand_.nextDouble()*distribution.get(distribution.size()-1).getDouble("upper");
		System.out.format("pick=%g\n", pick);
		for(int i = 0; i < distribution.size(); i++) {
			JSONObject obj = distribution.get(i);
			if(obj.getDouble("lower")<=pick && pick < obj.getDouble("upper"))
				return obj.getString("key");
		}
		return distribution.get(distribution.size()-1).getString("key");
	}
	private void GenerateDistribution(ArrayList<JSONObject> data, ArrayList<JSONObject> distribution) {
		distribution.clear();
		double total = 0.0;
		for(int i = 0; i < data.size(); i++) {
			JSONObject obj = new JSONObject();
			obj.put("lower", total);
			total += data.get(i).getDouble("probability");
			obj.put("upper", total);
			obj.put("key", data.get(i).getString("key"));
			distribution.add(obj);
		}
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
