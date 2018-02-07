package mail;

import org.json.JSONObject;

import assistantbot.MyAssistantUserData;
import managers.Replier;
import util.MyBasicBot;

public class KMailReplier implements MailReplier, Replier {
	String tmail_ = null;
	MyBasicBot bot_ = null;
	Long chatID_;
	MyMail mymail_ = null;
	MyAssistantUserData userData_ = null;
	public KMailReplier(String tmail, MyBasicBot bot, Long chatID, MyAssistantUserData userData, MyMail mymail)
	{
		bot_ = bot;
		tmail_ = tmail;
		chatID_ = chatID;
		userData_ = userData;
		mymail_ = mymail;
	}
	@Override
	public void onMessageArrived(JSONObject message) throws Exception {
		bot_.sendMessage(String.format("new mail from %s: %s\n",tmail_.substring(0,tmail_.indexOf("@")).toUpperCase(),
				message.getString("subject")), chatID_);
		if(false)
			mymail_.replyTo(message,"this is my BODY\ndo you like it?");
	}

	@Override
	public String processReply(int messageID, String msg) {
		// TODO Auto-generated method stub
		return null;
	}

}
