package assistantbot;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
	void sendMessage(String msg);
	Scheduler getScheduler();
	public abstract int sendMessage(String string, MyManager testManager) throws Exception;
	public abstract int sendFile(String fn) throws TelegramApiException;
	public abstract int sendMessageWithKeyBoard(String msg, List<List<InlineKeyboardButton>> makePerCatButtons);
}
