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
import util.UserCollection;

public interface ResourceProvider {
	/**
	 * 
	 * @param msg
	 * @param categories
	 * @return message id
	 * @deprecated
	 */
	abstract public int sendMessageWithKeyBoard(String msg, JSONArray categories);
	/**
	 * @deprecated
	 * @return
	 */
	MongoClient getMongoClient();
	int sendMessage(String msg);
	Scheduler getScheduler();
	public abstract int sendMessage(String msg, Transformer<String,String> t) throws Exception;
	public abstract int sendFile(String fn) throws Exception;
	/**
	 * 
	 * @param msg
	 * @param makePerCatButtons
	 * @return
	 * @deprecated use {@link #sendMessageWithKeyBoard(String, JSONArray)} instead
	 */
	public abstract int sendMessageWithKeyBoard(String msg, List<List<InlineKeyboardButton>> makePerCatButtons);
	public MongoCollection<Document> getCollection(UserCollection name);
	/**
	 * @deprecated should be replaced with corresponding call to getManagerSettingsObject()
	 * @return
	 */
	public JSONObject getUserObject();
	public JSONObject getManagerSettingsObject(String classname);
	public ResourceProvider setManagerSettingsObject(String classname,String key, Object val);
	abstract public int sendMessageWithKeyBoard(String msg, Map<String, Object> map, Transformer<Object,String> me);
}
