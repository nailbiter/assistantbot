package assistantbot;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.Transformer;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import it.sauronsoftware.cron4j.Scheduler;
import managers.MyManager;
import util.Message;
import util.UserCollection;

public interface ResourceProvider {
	/**
	 * 
	 * @param msg
	 * @param categories
	 * @return message id
	 * @deprecated
	 */
	abstract public int sendMessageWithKeyBoard(Message msg, JSONArray categories);
	/**
	 * @deprecated
	 * @return
	 */
	MongoClient getMongoClient();
	int sendMessage(Message msg);
	Scheduler getScheduler();
	public abstract int sendMessage(Message msg, Transformer<String,Message> t) throws Exception;
	public abstract int sendFile(String fn) throws Exception;
	public MongoCollection<Document> getCollection(UserCollection name);
	/**
	 * @deprecated should be replaced with corresponding call to getManagerSettingsObject()
	 * @return
	 */
	public JSONObject getUserObject();
	public JSONObject getManagerSettingsObject(String classname);
	public ResourceProvider setManagerSettingsObject(String classname,String key, Object val);
	public int sendMessageWithKeyBoard(Message msg, Map<String, Object> map, Transformer<Object,Message> me);
	/**
	 * remote procedure call
	 * @param managerName
	 * @param methodName
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	public Object rpc(String managerName, String methodName, Object arg) throws Exception;
}
