/**
 * 
 */
package mail;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import it.sauronsoftware.cron4j.Scheduler;
import managers.MailManager;
import util.KeyRing;

/**
 * @author nailbiter
 * Class for mail handling
 */
public class MyMail {
	private Logger logger_ = Logger.getLogger(this.getClass().getName());
	Folder fol_ = null;
	Store st_ = null;
	String ID;
	Scheduler scheduler_;
	String user;
	Hashtable<String, MailReplier> repliers_ = new Hashtable<String,MailReplier>();
	public MyMail(String mail, String host, int port, String password, String target_folder, Scheduler scheduler) throws Exception
	{
		scheduler_ = scheduler;
		user = mail.substring(0, mail.indexOf("@"));
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
	public void setReplier(String mail_from,MailReplier mr) { this.repliers_.put(mail_from, mr); }
	protected boolean isFrom(Message m,String tmail) throws Exception
	{
		logger_.info(String.format("compare subj=%s, tmail=%s", m.getSubject(),tmail));
		Address[] senders = m.getFrom();
		for(int i = 0; i < senders.length; i++)
		{
			logger_.info(String.format("\tsender=%s", senders[i].toString()));
			if(senders[i].toString().contains(tmail))
				return true;
		}
		return false;
	}
	protected MessageCountListener getMessageCountListener() {
		return new MessageCountListener() {
			@Override
			public void messagesAdded(MessageCountEvent ev) {
			    Message[] msgs = ev.getMessages();

			    // Just dump out the new messages
			    for (int i = 0; i < msgs.length; i++) {
					try {
						Iterator<String> itr = repliers_.keySet().iterator();
						while (itr.hasNext()) { 
							String adr = itr.next();
							if(isFrom(msgs[i],adr))
							{
								repliers_.get(adr).onMessageArrived(msgs[i]);
							}
						}
					} catch (Exception ioex) { 
					    ioex.printStackTrace();	
					}
			    }
			}

			@Override
			public void messagesRemoved(MessageCountEvent arg0) {}
		};
	}
	public void reschedule(int freq) throws Exception
	{
		scheduler_.reschedule(ID, getSchedulingPattern(freq));
	}
	protected static String getSchedulingPattern (int min) throws Exception
	{
		if(min <= 0) throw new Exception("min<=0: "+min);
		return (min==1) ? "* * * * *" : String.format("*/%d * * * *", min);
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
	static boolean isSeen(Flags flags)
	{
		return flags.toString().contains("\\Seen");
	}
}
