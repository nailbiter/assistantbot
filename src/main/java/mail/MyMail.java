/**
 * 
 */
package mail;

import java.net.Authenticator;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import it.sauronsoftware.cron4j.Scheduler;
import managers.MailManager;
import managers.Replier;
import util.KeyRing;

/**
 * @author nailbiter
 * Class for mail handling
 */
public class MyMail implements Replier{
	private Logger logger_ = Logger.getLogger(this.getClass().getName());
	Folder fol_ = null;
	Store st_ = null;
	String ID;
	Scheduler scheduler_;
	String user,mail_;
	Hashtable<String, MailReplier> repliers_ = new Hashtable<String,MailReplier>();
	List<Replier>  messageRepliers_ = new ArrayList<Replier>();
	Session sess_ = null;
	public MyMail(String mail, String host, int port, String password, String target_folder, Scheduler scheduler) throws Exception
	{
		mail_ = mail;
		scheduler_ = scheduler;
		user = mail.substring(0, mail.indexOf("@"));
		Properties props = System.getProperties();
		sess_ = Session.getInstance(props, null);
//		sess.setDebug(true);

		st_ = sess_.getStore("imaps");
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
	public void setMailReplier(String mail_from,MailReplier mr) { this.repliers_.put(mail_from, mr); }
	public void setMessageReplier(Replier r)	{this.messageRepliers_.add(r);}
	protected boolean isFrom(Message m,String tmail) throws Exception
	{
		//logger_.info(String.format("compare subj=%s, tmail=%s", m.getSubject(),tmail));
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
	private static boolean isSeen(Flags flags) {return flags.toString().contains("\\Seen");}
	@Override
	public String processReply(int messageID, String msg) {
		String res = null;
		for(int i = 0; i < this.messageRepliers_.size(); i++)
			if((res = this.messageRepliers_.get(i).processReply(messageID, msg)) != null)
				return res;
		return null;
	}
	public void replyTo(Message m) throws MessagingException {
		logger_.info("replyTo 1");
		
		mymain();
		if(true)return;
		
		Transport t = null;
		try {
			Message replyMessage = m.reply(false);
			String to = InternetAddress.toString(m
			         .getRecipients(Message.RecipientType.TO));
			replyMessage.setFrom(new InternetAddress(to));
			replyMessage.setText("Thanks");
			replyMessage.setReplyTo(m.getReplyTo());
			
			//Transport.send(replyMessage);
			t = sess_.getTransport("smtp");
			t.connect("leontiev", "k2h-vdE-LE4-EGg");
			t.sendMessage(replyMessage,replyMessage.getAllRecipients());
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
			logger_.info(String.format("err: %s", e.getMessage()));
		}
		finally {
			t.close();
		}
		
		logger_.info("sent succesfully");
	}
   public static void mymain() {    
	      // Recipient's email ID needs to be mentioned.
	      String to = "alozz1991@gmail.com";

	      // Sender's email ID needs to be mentioned
	      String from = "leontiev@ms.u-tokyo.ac.jp";

	      // Assuming you are sending email from localhost
	      String host = "mail.ms.u-tokyo.ac.jp";

	      // Get system properties
	      Properties props = System.getProperties();

	      // Setup mail server
	      props.setProperty("mail.smtp.host", host);
	      props.put("mail.smtp.port", 587);
	      props.put("mail.smtp.starttls.enable", true);
	      props.setProperty("mail.user", "leontiev");
	      props.setProperty("mail.password", "k2h-vdE-LE4-EGg");
	      /*Authenticator auth = new SMTPAuthenticator();
	      Session session = Session.getDefaultInstance(props, auth);*/

	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(props);

	      try {
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

	         // Set Subject: header field
	         message.setSubject("This is the Subject Line!");

	         // Now set the actual message
	         message.setText("This is actual message");

	         // Send message
	         Transport.send(message);
	         System.out.println("Sent message successfully....");
	      } catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	   }
}
