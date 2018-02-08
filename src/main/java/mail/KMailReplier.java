package mail;

import java.util.Hashtable;

import org.json.JSONObject;

import assistantbot.MyAssistantUserData;
import managers.MailManager;
import managers.OptionReplier;
import managers.Replier;
import util.MyBasicBot;

public class KMailReplier implements MailReplier{
	String tmail_ = null;
	MyBasicBot bot_ = null;
	Long chatID_;
	MyMail mymail_ = null;
	MyAssistantUserData userData_ = null;
	TemplateManager templatemanager_ = null;
	public KMailReplier(String tmail, MyBasicBot bot, Long chatID, MyAssistantUserData userData, MyMail mymail)
	{
		bot_ = bot;
		tmail_ = tmail;
		chatID_ = chatID;
		userData_ = userData;
		mymail_ = mymail;
		templatemanager_ = mymail_.getTemplateManager();
	}
	Hashtable<Integer,JSONObject> messages_ = new Hashtable<Integer,JSONObject>();
	@Override
	public void onMessageArrived(JSONObject message) throws Exception {
		int id = 
				/*bot_
				 * .sendMessage(
				 * String.format("new mail from %s: %s\n",tmail_.substring(0,tmail_.indexOf("@")).toUpperCase(),
				message.getString("subject")), chatID_)*/
				userData_.sendMessageWithKeyBoard(
						String.format("new mail from %s: %s\n",tmail_.substring(0,tmail_.indexOf("@")).toUpperCase(),
								message.getString("subject")),templatemanager_.getTemplateNames())
				;
		messages_.put(id, message);
	}

	@Override
	public String processReply(int messageID, String msg) {
		/*if( !messages_.containsKey(messageID) )
			return null;
		
		try{
			String[] tokens =  msg.split(" ");
			if( !tokens[0].equals("/" + MailManager.MAILREPLY) )
				return "unknown command";
			mymail_.replyTo(messages_.get(messageID),"this is my BODY\ndo you like it?");
			messages_.remove(messageID);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		return null;
	}
	@Override
	public String optionReply(String option, Integer msgID) throws Exception {
		if( !messages_.containsKey(msgID) )
			return null;
		JSONObject msg = messages_.get(msgID);
		MailTemplate template = this.templatemanager_.getMailTemplate(option);  
		mymail_.replyTo(msg,template.toString());
		messages_.remove(msgID);
		return template.toString();
	}

}
