package util;
import java.util.ArrayList;
/**
 * @author nailbiter
 */
import java.util.Hashtable;
import java.util.List;

import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import managers.OptionReplier;

/**
 * @author nailbiter
 *
 */
public interface UserData {
	abstract public void Update(JSONObject res);
	abstract public String processUpdateWithCallbackQuery(String call_data, int message_id) throws Exception;
}
