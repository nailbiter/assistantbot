package managers;

import static java.util.Arrays.asList;
import static util.Util.GetRebootFileName;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.MongoClient;

import assistantbot.ResourceProvider;
import managers.misc.NoteMaker;
import managers.misc.RandomSetGenerator;
import util.AssistantBotException;
import util.JsonUtil;
import util.KeyRing;
import util.Util;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrdered.ArgTypes;
import util.scriptapps.JsApp;
import util.scriptapps.ScriptApp;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;
import util.scripthelpers.ScriptHelper;

public class MiscUtilManager extends AbstractManager {
	Random rand_ = new Random();
	private final static String[] ALPHABETS = new String[] {
			"abcdefghijklmnopqrstuvwxyz",
			"abcdefghijklmnopqrstuvwxyz".toUpperCase(),
			"0123456789"
	};
	private Hashtable<String,Object> hash_ = new Hashtable<String,Object>();
	TrelloAssistant ta_;
	String tasklist_ = null;
	private static final String TASKLISTNAME = "todo";
	NoteMaker nm_ = null;
	private ResourceProvider rp_;
	private ScriptApp sa_;
	
	public MiscUtilManager(ResourceProvider rp) throws Exception {
		super(GetCommands());
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		try{
			tasklist_ = ta_.findListByName(managers.habits.Constants.HABITBOARDID, TASKLISTNAME);
		} catch(Exception e) {
			e.printStackTrace(System.err);
		}
		nm_ = new NoteMaker(rp);
		rp_ = rp;
		sa_ = new JsApp(Util.getScriptFolder()+"gendistrib",null, 
				new ScriptHelper() {
					@Override
					public String execute(String arg) throws Exception {
						return null;
					}
					@Override
					public void setInvocable(Invocable inv) {
					}
		});
	}
	public static JSONArray GetCommands() throws Exception {
		return new JSONArray()
				.put(new ParseOrderedCmd("rand", "return random",
					new ParseOrderedArg("key",ParseOrdered.ArgTypes.integer)
						.makeOpt(),
					new ParseOrderedArg("charset",ParseOrdered.ArgTypes.string)
						.useDefault("aA")
				))
				.put(new ParseOrderedCmd("misc","misc"
						,new ParseOrderedArg("command",ArgTypes.string)
							.useDefault("help")
						,new ParseOrderedArg("keys",ArgTypes.remainder)
							.useDefault("")))
				.put(new ParseOrderedCmd("randset","return randomly generated set",
						new ParseOrderedArg("size",ArgTypes.integer)))
				.put(new ParseOrderedCmd("note","make note",
						new ParseOrderedArg("notecontent",
								ParseOrdered.ArgTypes.remainder)))
				.put(new ParseOrderedCmd("ttask", "make new task", 
						new ParseOrderedArg("task",
								ParseOrdered.ArgTypes.remainder)));
	}
	public String misc(JSONObject obj) throws AssistantBotException {
		String command = obj.getString("command")
				,keys = obj.getString("keys");
		if( command.equals("timer") ) {
			return rand(new JSONObject()
					.put("key", 10)
					.put("charset", "aA0"));
		} else {
			throw new AssistantBotException(AssistantBotException.Type.MISCUTILMANAGER
					,String.format("command \"%s\" unknown", command));
		}
	}
	public String note(JSONObject obj) {
		String noteContent = obj.getString("notecontent");
		nm_.makeNote(noteContent);
		return String.format("made note \"%s\"", noteContent);
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	public String randset(JSONObject obj) throws JSONException, Exception {
		final ArrayList<JSONObject> data = new ArrayList<JSONObject>();
		JSONArray distrib = new JSONArray(sa_.runCommand("updatedistrib"));
		for(Object o:distrib)
			data.add((JSONObject)o);
		
		ArrayList<String> res = RandomSetGenerator.MakeRandomSet(data,obj.getInt("size"));
		return String.format("%s", res.toString());
	}
	public String rand(JSONObject obj){
		int len;
		if(obj.has("key"))
			len = obj.getInt("key");
		else
			len = (int)hash_.get("key");
		hash_.put("key", len);
		
		StringBuilder sb = new StringBuilder();
		String alphabet = "";
		String charset = obj.getString("charset");
		System.err.format("charset=\"%s\"\n", charset);
		for(int i = 0; i < charset.length(); i++) {
			char c = charset.charAt(i);
			for(String a:ALPHABETS) {
				System.err.format("\t checking \"%s\"\n", a);
				if( a.indexOf(c) >= 0 ) {
					System.err.format("\t adding \"%s\"", a);
					alphabet += a;
					break;
				}
			}
		}
		System.err.format("alphabet=\"%s\"\n", alphabet);
		
		for(int i = 0; i < len; i++)
			sb.append(alphabet.charAt(rand_.nextInt(alphabet.length())));
		
		return sb.toString();
	}
	public String ttask(JSONObject obj) throws Exception {
		String task = obj.getString("task");
		JSONObject card = ta_.addCard(tasklist_, new JSONObject().put("name", task));
		JsonUtil.FilterJsonKeys(card, new JSONArray().put("name").put("shortUrl"));
		rp_.sendMessage(String.format("added task\n%s", Util.JsonObjectToTable(card)));
		return "";
	}
}
