package assistantbot;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.Transformer;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

import it.sauronsoftware.cron4j.Scheduler;
import managers.AbstractManager;
import managers.MyManager;
import util.AssistantBotException;
import util.JsonUtil;
import util.SettingCollection;
import util.UserCollection;
import util.Util;
import util.db.MongoUtil;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;
import util.parsers.StandardParserInterpreter;

public class BasicUserData extends AbstractManager implements ResourceProvider {
	protected List<MyManager> managers_ = new ArrayList<MyManager>();
	protected StandardParserInterpreter parser_;
	protected JSONObject userObject_ = null;
	protected final boolean isSingleUser_;
	/**
	 * FIXME: should it be a singleton?
	 */
	protected Scheduler scheduler_ = new Scheduler();
	protected Logger logger_;
	private Hashtable<Integer, Transformer<String, String>> messageRepliers_ = new Hashtable<Integer,Transformer<String, String>>();
	protected BasicUserData(boolean isSingleUser) throws JSONException, Exception {
		super(GetCommands(isSingleUser));
		isSingleUser_ = isSingleUser;
		logger_ = Logger.getLogger(this.getClass().getName());
		if(isSingleUser)
			userObject_ = Util.GetDefaultUser();
	}
	private static JSONArray GetCommands(boolean isSingleUser) throws AssistantBotException {
		JSONArray res = new JSONArray()
				.put(new ParseOrderedCmd("help", "display this help message"))
				.put(new ParseOrderedCmd("managers", "operations on managers"
						,new ParseOrderedArg("managername",ArgTypes.string)
							.makeOpt()
						,new ParseOrderedArg("command",ArgTypes.string)
							.useDefault("")))
				;
		if( !isSingleUser )
			res.put(new ParseOrderedCmd("start", "display this message"
					,new ParseOrderedArg("login",ArgTypes.string)
						.makeOpt()
					,new ParseOrderedArg("pass",ArgTypes.string)
						.useDefault("")));
		if( Util.Gss(Util.EnvironmentParameter.CLIENT).equals("terminal") ) {
			res
				.put(new ParseOrderedCmd("keyboard","answer to last keyboard"
					,new ParseOrderedArg("num",ArgTypes.integer)))
				.put(new ParseOrderedCmd("msgreply","answer to message"
					,new ParseOrderedArg("msgid",ArgTypes.integer)
					,new ParseOrderedArg("msgcontent",ArgTypes.remainder)))
			;
		}
		return res;
	}
	public String managers(JSONObject obj) throws Exception {
		if( !obj.has("managername") ) {
			StringBuilder sb = new StringBuilder();
			for(MyManager am:managers_) {
				sb.append(String.format("%s\n", am));
			}
			return sb.toString();
		} else {
			String pref = obj.getString("managername");
			MyManager manager = null;
			for(MyManager am:managers_) {
				if(am.toString().startsWith(pref))
					manager = am;
			}
			if( manager == null ) {
				return String.format("no manager starting with \"%s\"", pref);
			} else {
				String command = obj.getString("command");
				if( command.equals("set") ) {
					manager.set();
					return "";
				} else {
					return String.format("no command \"%s\" on \"%s\"", 
							obj.getString("command"),manager.toString());
				}
			}
		}
	}
	public String start(JSONObject obj) throws Exception {
		if(isSingleUser_)
			throw new Exception(String.format("cannot call \"%s\" on single-user", "start"));
		if( !obj.has("login") ) {
			return unlogin();
		} else {
			return login(obj.getString("login"),obj.getString("pass"));
		}
	}
	public String help(JSONObject obj) {
		return parser_.getHelpMessage();
	}
	protected String unlogin() throws JSONException, Exception {
		return null;
	}
	protected String login(String username, String password) throws JSONException, Exception {
		return null;
	}
	@Override
	public int sendMessageWithKeyBoard(String msg, JSONArray categories) {
		return 0;
	}
	@Override
	public MongoClient getMongoClient() {
		return null;
	}
	@Override
	public int sendMessage(String msg) {
		return 0;
	}
	@Override
	public Scheduler getScheduler() {
		return scheduler_;
	}
	@Override
	public int sendFile(String fn) throws Exception {
		return 0;
	}
	@Override
	public int sendMessageWithKeyBoard(String msg, List<List<InlineKeyboardButton>> makePerCatButtons) {
		return 0;
	}
	@Override
	public MongoCollection<Document> getCollection(UserCollection name) {
		return null;
	}
	@Override
	public JSONObject getUserObject() {
		return userObject_;
	}
	@Override
	public JSONObject getManagerSettingsObject(String classname) {
		System.err.format("getting param object for %s\n", classname);
		
		JSONObject res = new JSONObject();
		ArrayList<MongoCollection<Document>> colls = new ArrayList<MongoCollection<Document>>();
			colls.add(MongoUtil.GetSettingCollection(this, SettingCollection.PARAMS));
			colls.add(getCollection(UserCollection.PARAMS));
			
		for(MongoCollection<Document> coll:colls) {
			JSONObject obj = MongoUtil.GetJsonObjectFromDatabase(coll, "name", classname),
					parameters = new JSONObject();
			if(obj != null)
				parameters = obj.getJSONObject("parameter");
			JsonUtil.CopyIntoJson(res, parameters);
		}
		
		return res;
	}
	@Override
	public ResourceProvider setManagerSettingsObject(String classname,String key, Object val) {
		System.err.format("setting \"%s\"=\"%s\" for \"%s\"\n", key,val,classname);
		MongoCollection<Document> coll = getCollection(UserCollection.PARAMS);
		JSONObject obj = MongoUtil.GetJsonObjectFromDatabase(coll, "name", classname),
				parameter = new JSONObject();
		if( obj != null )
			parameter = obj.getJSONObject("parameter");
		parameter.put(key, val);
		
		coll.updateOne(Filters.eq("name",classname)
				, Updates.set("parameter", Document.parse(parameter.toString()))
				,new UpdateOptions().upsert(true));
		
		return this;
	}
	@Override
	public int sendMessageWithKeyBoard(String msg, Map<String, Object> map, Transformer<Object,String> me) {
		return sendMessageWithKeyBoard(msg,new JSONArray(map.keySet()));
	}
	@Override
	public int sendMessage(String msg, Transformer<String, String> t) throws Exception {
		int res = sendMessage(msg);
		messageRepliers_.put(res, t);
		return res;
	}
	@Override
	public String processReply(int messageID, String msg) {
		return messageRepliers_.get(messageID).transform(msg);
	}
}
