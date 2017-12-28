package util;
import java.util.Hashtable;

import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

public abstract class MyBasicBot extends TelegramLongPollingBot {
	public void onUpdateReceived(Update update) {
		// We check if the update has a message and the message has text
		if (update.hasMessage()) {
			try {
				SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
						.setChatId(update.getMessage().getChatId())
								.setText("<code>"+reply(update.getMessage())+"</code>");
				message.setParseMode("HTML");
				sendMessage(message); // Call method to send the message
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
	}
	abstract protected JSONObject parse(Message msg,UserData ud) throws Exception;
	abstract protected UserData createUserData(Long chatId); 
	java.util.Hashtable<Long, UserData> userData = new Hashtable<Long,UserData>();
	protected String reply(Message msg){
		try{
			if(true)
			{
				if(!this.userData.containsKey(msg.getChatId()))
					userData.put(msg.getChatId(), this.createUserData(msg.getChatId())); 
				JSONObject res = parse(msg,userData.get(msg.getChatId()));
				userData.get(msg.getChatId()).Update(res);
				return this.getResultAndFormat(res,userData.get(msg.getChatId()));
			}
			else
			{
				return msg.getText() + " " + msg.getChatId();
			}
		}
		catch (Exception e) {
	            BotLogger.error(this.getLogString(), e);
	            e.printStackTrace(System.out);
	            return "exception: "+e.getMessage();
	            //responseToUser = LocalisationService.getInstance().getString("errorFetchingWeather", language);
	    }
	}
	abstract protected String getResultAndFormat(JSONObject res,UserData ud) throws Exception;	
	protected String getHelpMessage()
	{
		return "help";
	}
	public String getLogString() {return getBotUsername();}
	public abstract String getBotUsername();
	public abstract String getBotToken();
}
