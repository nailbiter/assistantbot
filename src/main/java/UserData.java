/**
 * @author nailbiter
 */
import java.util.Hashtable;

import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegrambots.api.objects.Message;

/**
 * @author nailbiter
 *
 */
public interface UserData {
	public void Update(JSONObject res);
	/*String getLastTo(){ return lastTo; }
	String getLastFrom(){ return lastFrom; }*/
}
