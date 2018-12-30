package assistantbot;

import java.util.List;

import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import com.mongodb.MongoClient;

import it.sauronsoftware.cron4j.Scheduler;
import managers.MyManager;

public interface ResourceProvider {
	/**
	 * 
	 * @param msg
	 * @param categories
	 * @return message id
	 */
	abstract public int sendMessageWithKeyBoard(String msg, JSONArray categories);
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
	/**
	 * @deprecated
	 * @return
	 */
	public abstract long getChatId();
	public abstract String getDbName();
}
