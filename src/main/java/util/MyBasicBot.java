package util;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.logging.BotLogger;

import com.mongodb.MongoClient;

import managers.MyManager;

public abstract class MyBasicBot extends TelegramLongPollingBot {
	private Logger logger_; 
	protected MongoClient mongoClient = null;
	public MyBasicBot()
	{
		logger_ = Logger.getLogger(this.getClass().getName());
	}
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
		try 
		{
			// We check if the update has a message and the message has text
			if (update.hasMessage()) {
				SendMessage message = new SendMessage();;
				String reply = null;
				if(update.getMessage().isReply())
				{
					reply = this.processReply(update);
				}
				else
				{
					reply = reply(update.getMessage());
				}
				
				message.setText(toHTML(reply));
				message.setChatId(update.getMessage().getChatId());								
				message.setParseMode("HTML");
				
				execute(message); // Call method to send the message
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
		
		String reply = toHTML(ud.processUpdateWithCallbackQuery(call_data, message_id));
		EditMessageText emt = new EditMessageText()
				.setChatId(chat_id)
				.setMessageId(message_id)
				.setText(reply)
				.setParseMode("HTML");
		execute(emt);
		/*SendMessage message = new SendMessage()
				.setChatId(chat_id)
				.setText();
		message.setParseMode("HTML");
		sendMessage(message); // Call method to send the message
		*/
	}
	private String processReply(Update update) throws Exception {
		int replyID = update.getMessage().getReplyToMessage().getMessageId();
		System.out.println("reply id: "+replyID);
		String reply = this.waitingForReply
			.get(update.getMessage().getChatId())
			.get(replyID)
			.processReply(replyID,update.getMessage().getText());
		this.waitingForReply.get(update.getMessage().getChatId()).remove(replyID);
		return reply;
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
	Hashtable<Long,Hashtable<Integer,MyManager>> waitingForReply = new Hashtable<Long,Hashtable<Integer,MyManager>>();
	/*
	 * expect reply
	 */
	public int sendMessage(String msg,Long chatID_,MyManager whom) throws Exception
	{
		SendMessage message = new SendMessage()
				.setChatId(chatID_)
						.setText(toHTML(msg));
		message.setParseMode("HTML");
		Message res = execute(message);
		if(!this.waitingForReply.containsKey(chatID_))
			this.waitingForReply.put(chatID_, new Hashtable<Integer,MyManager>());
		this.waitingForReply.get(chatID_).put(res.getMessageId(), whom);
		return res.getMessageId();
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
	public MongoClient getMongoClient() {
		return mongoClient;
	}
}
