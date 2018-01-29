package util;
import java.util.ArrayList;
/**
 * @author nailbiter
 */
import java.util.Hashtable;
import java.util.List;

import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import managers.OptionReplier;

import org.telegram.telegrambots.api.objects.Message;

/**
 * @author nailbiter
 *
 */
public abstract class UserData {
	abstract public void Update(JSONObject res);
	public String processUpdateWithCallbackQuery(String call_data, int message_id)  {
		return null;
	}
}
