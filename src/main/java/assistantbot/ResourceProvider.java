package assistantbot;

import java.util.List;

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
	 */
	abstract public int sendMessageWithKeyBoard(String msg, JSONArray categories);
	/**
	 * @deprecated
	 * @return
	 */
	MongoClient getMongoClient();
	int sendMessage(String msg);
	Scheduler getScheduler();
	public abstract int sendMessage(String string, MyManager testManager) throws Exception;
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
}
