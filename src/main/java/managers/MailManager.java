/**
 * 
 */
package managers;


import org.json.JSONArray;
import org.json.JSONObject;

import it.sauronsoftware.cron4j.Scheduler;
import util.MyBasicBot;
import util.MyManager;

import java.io.IOException;
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
public class MailManager implements MyManager {
	protected Long chatID_ = null;
	MyBasicBot bot_ = null;
	Scheduler scheduler_ = null;
	Folder fol_ = null;
	Store st_ = null;
	String ID = null;
	//protected static String TOSHIMAIL = "toshi@ms.u-tokyo.ac.jp";
	protected static String TOSHIMAIL = "toshi@ms.u-tokyo.ac.jp";
	public MailManager(Long chatID, MyBasicBot bot, Scheduler scheduler) throws Exception{
		this.chatID_ = chatID;
		this.bot_ = bot;
		this.scheduler_ = scheduler;
		
		String host = "mail.ms.u-tokyo.ac.jp";
		int port = 993;
		String user = "leontiev";
		String password = "pa$$w0rD";
		String target_folder = "INBOX";

		Properties props = System.getProperties();
		Session sess = Session.getInstance(props, null);
//		sess.setDebug(true);

		st_ = sess.getStore("imaps");
		st_.connect(host, port, user, password);
		fol_ = st_.getFolder(target_folder);
		if(!fol_.exists())
			throw new Exception(String.format("%s is not exist.", target_folder));
		fol_.open(Folder.READ_ONLY);
		fol_.addMessageCountListener(this.getMessageCountListener());
		ID = scheduler.schedule("* * * * *", 
				new Runnable() {public void run() {try {
			fol_.getMessageCount();
		} catch (MessagingException e) {
			e.printStackTrace();
		}}});
	}
	protected MessageCountListener getMessageCountListener() {
		return new MessageCountListener() {
			@Override
			public void messagesAdded(MessageCountEvent ev) {
			    Message[] msgs = ev.getMessages();
			    StringBuilder sb = new StringBuilder();

			    // Just dump out the new messages
			    for (int i = 0; i < msgs.length; i++) {
					try {
						if(isFromK(msgs[i]))
							sb.append(String.format("new mail from K!: %s\n", msgs[i].getSubject()));
					} catch (Exception ioex) { 
					    ioex.printStackTrace();	
					}
			    }
			    if( sb.length() > 0 )
			    		bot_.sendMessage(sb.toString(), chatID_);
			}

			@Override
			public void messagesRemoved(MessageCountEvent arg0) {}
		};
	}
	protected boolean isFromK(Message m) throws Exception
	{
		Address[] senders = m.getFrom();
		for(int i = 0; i < senders.length; i++)
			if(senders[i].toString().contains(TOSHIMAIL))
				return true;
		return false;
	}
	/* (non-Javadoc)
	 * @see util.MyManager#getResultAndFormat(org.json.JSONObject)
	 */
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("mailfreq")==0)
			{
				scheduler_.reschedule(ID, MailManager.getSchedulingPattern(res.getInt("freq")));
				return String.format("set freq to %d min", res.getInt("freq"));
			}
		}
		return null;
	}
	protected static String getSchedulingPattern (int min) throws Exception
	{
		if(min <= 0) throw new Exception("min<=0: "+min);
		return (min==1) ? "* * * * *" : String.format("*/%d * * * *", min);
	}
	static boolean isSeen(Flags flags)
	{
		return flags.toString().contains("\\Seen");
	}

	/* (non-Javadoc)
	 * @see util.MyManager#gotUpdate(java.lang.String)
	 */
	@Override
	public String gotUpdate(String data) throws Exception {
		return null;
	}
	@Override
	protected void finalize()
	{
		try {
			fol_.close(false);
			st_.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	@Override
	public JSONArray getCommands() {
		return new JSONArray("[{\"name\":\"mailfreq\",\"args\":[{\"name\":\"freq\",\"type\":\"int\"}],\"help\":\"set mailbox check freq to MIN\"}]");
	}
}
