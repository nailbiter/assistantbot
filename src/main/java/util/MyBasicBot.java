package util;
import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import managers.MyManager;

public abstract class MyBasicBot extends TelegramLongPollingBot {
	private Logger logger_; 
	public MyBasicBot()
	{
		logger_ = Logger.getLogger(this.getClass().getName());
	}
	protected static String ToHTML(String arg)
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
				
				sendHtmlMessage(message,update,reply);
			}
			else if(update.hasCallbackQuery())
				this.processUpdateWithCallbackQuery(update);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void sendHtmlMessage(SendMessage message, Update update, String reply) throws TelegramApiException {
//		reply = Util.CheckMessageLen(reply);
		
		message.setText(ToHTML(reply));
		message.setChatId(update.getMessage().getChatId());								
		message.setParseMode("HTML");
		
		if(reply.length()>0)
			execute(message);
	}
	void processUpdateWithCallbackQuery(Update update) throws Exception
	{
		String call_data = update.getCallbackQuery().getData();
		int message_id = update.getCallbackQuery().getMessage().getMessageId();
		long chat_id = update.getCallbackQuery().getMessage().getChatId();
		UserData ud = this.userData.get(chat_id);
		
		System.out.println("got call_data="+call_data);
		
		String reply = ToHTML(ud.processUpdateWithCallbackQuery(call_data, message_id));
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
		String reply = this.waitingForReply_
			.get(update.getMessage().getChatId())
			.get(replyID)
			.processReply(replyID,update.getMessage().getText());
		this.waitingForReply_.get(update.getMessage().getChatId()).remove(replyID);
		return reply;
	}
	abstract protected JSONObject interpret(Message msg,UserData ud) throws Exception;
	abstract protected UserData createUserData(Long chatId) throws JSONException, Exception; 
	protected java.util.Hashtable<Long, UserData> userData = 
			new Hashtable<Long,UserData>();
	protected String reply(Message msg){
		try{
			if(!this.userData.containsKey(msg.getChatId()))
				userData.put(msg.getChatId(), this.createUserData(msg.getChatId())); 
			JSONObject res = interpret(msg,userData.get(msg.getChatId()));
			userData.get(msg.getChatId()).Update(res);
			return this.getResultAndFormat(res,userData.get(msg.getChatId()));
		}
		catch (Exception e) {
	            BotLogger.error(this.getLogString(), e);
	            e.printStackTrace(System.err);
	            return String.format("e: %s", ExceptionUtils.getStackTrace(e));
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
	Hashtable<Long,Hashtable<Integer,MyManager>> waitingForReply_ = new Hashtable<Long,Hashtable<Integer,MyManager>>();
	/*
	 * expect reply
	 */
	public int sendMessage(String msg,Long chatID_,MyManager whom) throws Exception
	{
		SendMessage message = new SendMessage()
				.setChatId(chatID_)
						.setText(/*ToHTML*/(msg));
//		message.setParseMode("HTML");
		Message res = execute(message);
		if(!this.waitingForReply_.containsKey(chatID_))
			this.waitingForReply_.put(chatID_, new Hashtable<Integer,MyManager>());
		this.waitingForReply_.get(chatID_).put(res.getMessageId(), whom);
		return res.getMessageId();
	}
	public int sendFile(String fn,Long chatId) throws TelegramApiException {
		logger_.info(String.format("fn=%s", fn));
		SendDocument message = new SendDocument()
				.setChatId(chatId)
				.setDocument(new File(fn));
		return execute(message).getMessageId();
	}
	public int sendMessage(String msg,Long chatID_)
	{
		try 
		{
			SendMessage message = new SendMessage()
					.setChatId(chatID_)
							.setText(msg);
			return execute(message).getMessageId();
		}
		catch(Exception e){ 
			e.printStackTrace(System.out);
			return -1;
		}
	}
	/**
	 * 
	 * @param msg
	 * @param chatID_
	 * @param whom
	 * @param buttons
	 * @return message id
	 */
	public int sendMessageWithKeyBoard(String msg,Long chatID_,
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
			Message res = execute(message); 
			int id = res.getMessageId();
			logger_.info(String.format("return id=%d", id));
			return id;
		}
		catch(Exception e)
		{ 
			e.printStackTrace(System.out);
			return -1;
		}
	}
}
