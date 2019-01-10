package util;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.logging.BotLogger;

public abstract class MyBasicBot extends TelegramLongPollingBot {
	protected Logger logger_; 
	public MyBasicBot()
	{
		logger_ = Logger.getLogger(this.getClass().getName());
	}
	public void onUpdateReceived(Update update) {
		try 
		{
			// We check if the update has a message and the message has text
			if (update.hasMessage()) {
				SendMessage message = new SendMessage();;
				String reply = null;
				if(update.getMessage().isReply()) {
					reply = this.processReply(update);
				} else {
					reply = reply(update.getMessage());
				}
				
				Util.SendHtmlMessage(this,message,update,reply);
			}
			else if(update.hasCallbackQuery())
				this.processUpdateWithCallbackQuery(update);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	void processUpdateWithCallbackQuery(Update update) throws Exception
	{
		String call_data = update.getCallbackQuery().getData();
		int message_id = update.getCallbackQuery().getMessage().getMessageId();
		long chat_id = update.getCallbackQuery().getMessage().getChatId();
		UserData ud = this.userData.get(chat_id);
		
		System.out.println("got call_data="+call_data);
		
		String reply = Util.ToHTML(ud.processUpdateWithCallbackQuery(call_data, message_id));
		EditMessageText emt = new EditMessageText()
				.setChatId(chat_id)
				.setMessageId(message_id)
				.setText(reply)
				.setParseMode("HTML");
		execute(emt);
	}
	private String processReply(Update update) throws Exception {
		int replyID = update.getMessage().getReplyToMessage().getMessageId();
		System.out.println("reply id: "+replyID);
		return interpretReply(replyID,update.getMessage().getText(),this.userData.get(update.getMessage().getChatId()));
	}
	protected abstract String interpretReply(int replyID, String string, UserData userData2);
	abstract protected JSONObject interpret(Message msg,UserData ud) throws Exception;
	abstract protected UserData createUserData(Long chatId) throws JSONException, Exception; 
	protected java.util.Hashtable<Long, UserData> userData = 
			new Hashtable<Long,UserData>();
	abstract protected String getResultAndFormat(JSONObject res,UserData ud) throws Exception;	
	public String getLogString() {
		return getBotUsername();
	}
	public abstract String getBotUsername();
	public abstract String getBotToken();
	protected String reply(Message msg) {
		try{
			if(!this.userData.containsKey(msg.getChatId()))
				userData.put(msg.getChatId(), this.createUserData(msg.getChatId())); 
			JSONObject res = interpret(msg,userData.get(msg.getChatId()));
			userData.get(msg.getChatId()).Update(res);
			return this.getResultAndFormat(res,userData.get(msg.getChatId()));
		} catch (Exception e) {
	            BotLogger.error(this.getLogString(), e);
	            e.printStackTrace(System.err);
	            return String.format("e: %s", ExceptionUtils.getStackTrace(e));
	    }
	}
}
