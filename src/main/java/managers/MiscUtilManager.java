package managers;

import static java.util.Arrays.asList;
import static util.Util.GetRebootFileName;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
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
import util.parsers.SimpleParser;
import util.scripthelpers.ScriptHelper;

public class MiscUtilManager extends AbstractManager {
	Random rand_ = new Random();
	private final static String[] ALPHABETS = new String[] {
			"abcdefghijklmnopqrstuvwxyz",
			"abcdefghijklmnopqrstuvwxyz".toUpperCase(),
			"0123456789"
	};
	private Hashtable<String,Object> hash_ = new Hashtable<String,Object>();
	String tasklist_ = null;
	NoteMaker nm_ = null;
	private ResourceProvider rp_;
	private ScriptApp sa_;
	private Hashtable<String,Date> timers_ = new Hashtable<String,Date>();
	private SimpleParser sp_; 
	
	public MiscUtilManager(ResourceProvider rp) throws Exception {
		super(GetCommands());
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
		sp_ = new SimpleParser()
				.makeNonStrict()
				.addCommand("help", "show this help message",this)
				.addCommand("timer", "timer help",this)
				;
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
							.useMemory()
						,new ParseOrderedArg("keys",ArgTypes.remainder)
							.useDefault("")))
				.put(new ParseOrderedCmd("randset","return randomly generated set",
						new ParseOrderedArg("size",ArgTypes.integer)))
				.put(new ParseOrderedCmd("note","make note",
						new ParseOrderedArg("notecontent",
								ParseOrdered.ArgTypes.remainder)))
				;
	}
	public void help(String x) {
		rp_.sendMessage(sp_.getHelpMessage());
	}
	public void timer(String keys) {
		if( keys.isEmpty() ) {
			TableBuilder tb = new TableBuilder()
					.addTokens("name_","time_");
			for( String k:timers_.keySet() )
				tb
					.addTokens(k
							,String.format("%s"
									,Util.milisToTimeFormat(
											System.currentTimeMillis()
											-timers_.get(k).getTime())));
			rp_.sendMessage(tb.toString());
		} else if(keys.equals("clear")) {
			String res = String.format("%s timers cleared",timers_.size());
			timers_.clear();
			rp_.sendMessage(res);
		} else if(keys.equals("new")) {
			String timerkey = createTimer();
			timers_.put(timerkey, new Date());
			rp_.sendMessage(String.format("created timer \"%s\"", timerkey)) ;
		} else {
			for( String k:timers_.keySet() )
				if(k.startsWith(keys))
					rp_.sendMessage(String.format("elapsed: %s"
							,Util.milisToTimeFormat(
									System.currentTimeMillis()
									-timers_.get(k).getTime())));
//			rp_.sendMessage(String.format("no timer stating with \"%s\"", keys));
		}
	}
	public String misc(JSONObject obj) throws JSONException, Exception {
		sp_.parse(obj.getString("command")
				+" "
				+obj.getString("keys"));
		return "";
	}
	private String createTimer() {
		return rand(new JSONObject()
				.put("key", 10)
				.put("charset", "aA0"));
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
}
