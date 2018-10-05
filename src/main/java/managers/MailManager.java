/**
 * 
 */
package managers;


import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.MyAssistantUserData;
import it.sauronsoftware.cron4j.Scheduler;
import mail.KMailReplier;
import mail.MailReplier;
import mail.MyMail;
import util.KeyRing;
import util.MyBasicBot;
import util.parsers.StandardParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.Address;
import javax.mail.Flags;

/**
 * @author nailbiter
 *
 */
public class MailManager implements MyManager, OptionReplier{
	protected Long chatID_ = null;
	MyBasicBot bot_ = null;
	MyAssistantUserData userData_ = null;
	MyMail mymail_ = null;
	public static final String MAILREPLY = "mailreply";
	public MailManager(Long chatID, MyBasicBot bot, Scheduler scheduler, MyAssistantUserData myAssistantUserData) throws Exception
	{
		String mail = KeyRing.get("memail");
		mymail_ = new MyMail(mail, "mail." + mail.substring(mail.indexOf("@")+1), 993,
				KeyRing.getMailPassword(), "INBOX",scheduler);
		this.chatID_ = chatID;
		this.bot_ = bot;
		this.userData_ = myAssistantUserData;
		
		addReplier(KeyRing.get("tmail"));
		//addReplier(KeyRing.get("megmail"));
	}
	private void addReplier(String m)
	{
		KMailReplier kmr = new KMailReplier(m,bot_,chatID_,userData_,mymail_);
		mymail_.setMailReplier(m,kmr);
		mymail_.setMessageReplier(kmr);
	}
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("mailfreq")==0)
			{
				mymail_.reschedule(res.getInt("freq"));
				return String.format("set freq to %d min", res.getInt("freq"));
			}
		}
		return null;
	}
	@Override
	public JSONArray getCommands() {
		JSONArray res = new JSONArray();
		res.put(AbstractManager.MakeCommand("mailfreq", "set mailbox check freq to MIN", 
				Arrays.asList(AbstractManager.MakeCommandArg("freq", StandardParser.ArgTypes.integer, 
						true))));
		//res.put(AbstractManager.makeCommand(MAILREPLY, "reply to mail",new ArrayList<JSONObject>()));
		return res;
	}
	@Override
	public String processReply(int messageID,String msg) {
		return mymail_.processReply(messageID, msg);
	}
	@Override
	public String optionReply(String option, Integer msgID) throws Exception {
		return mymail_.optionReply(option, msgID);
	}
}
