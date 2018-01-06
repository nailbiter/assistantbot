package util;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

public abstract class MyBasicBot extends TelegramLongPollingBot {
	protected static String toHTML(String arg)
	{
		return 
				"<code>"
				+arg
					.replaceAll("&", "&amp")
					.replaceAll("<", "&lt;")
					.replaceAll(">", "&gt;")
				+"</code>";
	}
	public void onUpdateReceived(Update update) {
		// We check if the update has a message and the message has text
		if (update.hasMessage()) {
			try {
				SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
						.setChatId(update.getMessage().getChatId())
								.setText(toHTML(reply(update.getMessage())));
				message.setParseMode("HTML");
				sendMessage(message); // Call method to send the message
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
		else if(update.hasCallbackQuery())
		{
			String call_data = update.getCallbackQuery().getData();
			System.out.println("got call_data="+call_data);
			//long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();
			try {
				SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
						.setChatId(chat_id)
								.setText("<code>"+lastWhoSentMessageWithKeyBoard.gotUpdate(call_data)+"</code>");
				message.setParseMode("HTML");
				sendMessage(message); // Call method to send the message
			} catch (Exception e) {
				e.printStackTrace(System.out);
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
	public void sendMessage(String msg,Long chatID_)
	{
		try 
		{
			SendMessage message = new SendMessage()
					.setChatId(chatID_)
							.setText(msg);
			sendMessage(message);
		}
		catch(Exception e){ e.printStackTrace(System.out); }
	}
	protected MyManager lastWhoSentMessageWithKeyBoard = null;
	public void sendMessageWithKeyBoard(String msg,Long chatID_,MyManager whom,
			List<List<InlineKeyboardButton>> buttons)
	{
		try 
		{
			SendMessage message = new SendMessage()
					.setChatId(chatID_)
							.setText(msg);
			
			InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
			markupInline.setKeyboard(buttons);
			message.setReplyMarkup(markupInline);
			this.lastWhoSentMessageWithKeyBoard = whom;
			sendMessage(message);
		}
		catch(Exception e){ e.printStackTrace(System.out); }
	}
}
