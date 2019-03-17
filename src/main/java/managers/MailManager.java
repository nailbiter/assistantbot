/**
 * 
 */
package managers;


import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.MyBasicBot;
import assistantbot.ResourceProvider;
import it.sauronsoftware.cron4j.Scheduler;
import mail.KMailReplier;
import mail.MailReplier;
import mail.MyMail;
import util.KeyRing;
import util.parsers.ParseOrdered;

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
public class MailManager extends AbstractManager implements OptionReplier, Replier{
	protected Long chatID_ = null;
	MyBasicBot bot_ = null;
	ResourceProvider userData_ = null;
	MyMail mymail_ = null;
	public static final String MAILREPLY = "mailreply";
	public MailManager(Long chatID, MyBasicBot bot, Scheduler scheduler, ResourceProvider myAssistantUserData) throws Exception
	{
		super(GetCommands());
		String mail = KeyRing.getString("memail");
		mymail_ = new MyMail(mail, "mail." + mail.substring(mail.indexOf("@")+1), 993,
				KeyRing.getMailPassword(), "INBOX",scheduler);
		this.chatID_ = chatID;
		this.bot_ = bot;
		this.userData_ = myAssistantUserData;
		
		addReplier(KeyRing.getString("tmail"));
	}
	private void addReplier(String m)
	{
		KMailReplier kmr = new KMailReplier(m,bot_,chatID_,userData_,mymail_);
		mymail_.setMailReplier(m,kmr);
		mymail_.setMessageReplier(kmr);
	}
	@Override
	public util.Message getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("mailfreq")==0)
			{
				mymail_.reschedule(res.getInt("freq"));
				return new util.Message(String.format("set freq to %d min", res.getInt("freq")));
			}
		}
		return null;
	}
	public static JSONArray GetCommands() {
		JSONArray res = new JSONArray();
		res.put(ParseOrdered.MakeCommand("mailfreq", "set mailbox check freq to MIN", 
				Arrays.asList(ParseOrdered.MakeCommandArg("freq", ParseOrdered.ArgTypes.integer, 
						true))));
		return res;
	}
	@Override
	public util.Message processReply(int messageID,String msg) {
		return mymail_.processReply(messageID, msg);
	}
	@Override
	public util.Message optionReply(String option, Integer msgID) throws Exception {
		return mymail_.optionReply(option, msgID);
	}
}
