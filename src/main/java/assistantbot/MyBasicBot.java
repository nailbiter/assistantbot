package assistantbot;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import util.TelegramUtil;
import util.UserData;
import util.Util;

public abstract class MyBasicBot extends TelegramLongPollingBot {
	protected Logger logger_; 
	public MyBasicBot() {
		logger_ = Logger.getLogger(this.getClass().getName());
	}
	public void onUpdateReceived(Update update) {
		try {
			// We check if the update has a message and the message has text
			if (update.hasMessage()) {
				SendMessage message = new SendMessage();;
				util.Message reply = null;
				if(update.getMessage().isReply()) {
					reply = processReply(update);
				} else {
					reply = reply(update.getMessage());
				}
				
				SendHtmlMessage(this,message,update,reply.getMessage());
			}
			else if(update.hasCallbackQuery())
				this.processUpdateWithCallbackQuery(update);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	protected static void SendHtmlMessage(TelegramLongPollingBot bot,SendMessage message, Update update, String reply) throws TelegramApiException {
		//		reply = Util.CheckMessageLen(reply);
				
				if(reply.isEmpty())
					return;
				
				message.setText(TelegramUtil.ToHTML(reply));
				message.setChatId(update.getMessage().getChatId());								
				message.setParseMode("HTML");
				
		//		if(reply.length()>0)
				bot.execute(message);
	}
	void processUpdateWithCallbackQuery(Update update) throws Exception
	{
		String call_data = update.getCallbackQuery().getData();
		int message_id = update.getCallbackQuery().getMessage().getMessageId();
		long chat_id = update.getCallbackQuery().getMessage().getChatId();
		UserData ud = this.userData.get(chat_id);
		
		System.out.println("got call_data="+call_data);
		
		String reply = TelegramUtil.ToHTML(processUpdateWithCallbackQuery(ud,call_data, message_id).getMessage());
		EditMessageText emt = new EditMessageText()
				.setChatId(chat_id)
				.setMessageId(message_id)
				.setText(reply)
				.setParseMode("HTML");
		execute(emt);
	}
	protected abstract util.Message processUpdateWithCallbackQuery(UserData ud,String call_data,int message_id) throws Exception;
	private util.Message processReply(Update update) throws Exception {
		int replyID = update.getMessage().getReplyToMessage().getMessageId();
		System.out.println("reply id: "+replyID);
		return interpretReply(replyID,update.getMessage().getText(),this.userData.get(update.getMessage().getChatId()));
	}
	protected abstract util.Message interpretReply(int replyID, String string, UserData userData2);
	abstract protected JSONObject interpret(Message msg,UserData ud) throws Exception;
	abstract protected UserData createUserData(Long chatId) throws JSONException, Exception; 
	protected java.util.Hashtable<Long, UserData> userData = 
			new Hashtable<Long,UserData>();
	abstract protected util.Message getResultAndFormat(JSONObject res,UserData ud) throws Exception;	
	public String getLogString() {
		return getBotUsername();
	}
	public abstract String getBotUsername();
	public abstract String getBotToken();
	protected util.Message reply(Message msg) {
		try{
			if( !this.userData.containsKey(msg.getChatId()) ) {
				userData.put(msg.getChatId(), this.createUserData(msg.getChatId()));
			}
			UserData ud = userData.get(msg.getChatId());
			JSONObject res = interpret(msg,ud);
			ud.Update(res);
			return getResultAndFormat(res,ud);
		} catch (Exception e) {
	            BotLogger.error(this.getLogString(), e);
	            e.printStackTrace(System.err);
	            return new util.Message(String.format("e: %s", ExceptionUtils.getStackTrace(e)));
	    }
	}
}
