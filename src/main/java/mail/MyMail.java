/**
 * 
 */
package mail;

import javax.mail.Authenticator;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.json.JSONException;
import org.json.JSONObject;

import it.sauronsoftware.cron4j.Scheduler;
import managers.MailManager;
import managers.OptionReplier;
import managers.Replier;
import util.KeyRing;

/**
 * @author nailbiter
 * Class for mail handling
 */
public class MyMail implements Replier, OptionReplier{
	private Logger logger_ = Logger.getLogger(this.getClass().getName());
	Folder fol_ = null;
	Store st_ = null;
	String ID;
	Scheduler scheduler_;
	String user_,mail_,host_;
	Hashtable<String, MailReplier> repliers_ = new Hashtable<String,MailReplier>();
	List<MailReplier>  messageRepliers_ = new ArrayList<MailReplier>();
	Session sess_ = null;
	String password_ = null;
	TemplateManager templatemanager_ = new TemplateManager();
	TemplateManager getTemplateManager() { return this.templatemanager_; }
	public MyMail(String mail, String host, int port, String password, String target_folder, Scheduler scheduler) throws Exception
	{
		mail_ = mail;
		host_ = host;
		password_ = password;
		scheduler_ = scheduler;
		user_ = mail.substring(0, mail.indexOf("@"));
		Properties props = System.getProperties();
		sess_ = Session.getInstance(props, null);

		st_ = sess_.getStore("imaps");
		st_.connect(host, port, user_, password);
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
	public void setMessageReplier(MailReplier r)	{this.messageRepliers_.add(r);}
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
								repliers_.get(adr).onMessageArrived(MsgToJSON(msgs[i]));
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
	Hashtable<Integer,Message> msgs_ = new Hashtable<Integer,Message>();
	Random rand_ = new Random();
	private JSONObject MsgToJSON(Message m) throws Exception
	{
		JSONObject res = new JSONObject();
		int id = rand_.nextInt();
		msgs_.put(id, m);
		
		return res
				.put("id", id)
				.put("subject", m.getSubject());
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
	@Override
	public String optionReply(String option, Integer msgID) throws Exception {
		String res = null;
		for(int i = 0; i < this.messageRepliers_.size(); i++)
			if((res = this.messageRepliers_.get(i).optionReply(option, msgID)) != null)
				return res;
		return null;
	}
	public void replyTo(JSONObject message,String body) throws Exception {
		inmain(msgs_.get(message.getInt("id")),body);
	}
   public void inmain(Message m, String string) throws Exception {
    		String  to, subject = null, from = mail_, 
    			cc = KeyRing.get("memail"), bcc = null, url = null;
    		String mailhost = null;
    		String mailer = "msgsend";
    		String file = null;
    		String protocol = null, host = null, user = null, password = password_;
    		String record = null;	// name of folder in which to record mail
    		boolean debug = false;

		if(true)
			to = m.getFrom()[0].toString();
		else
			to = KeyRing.get("megmail");
    		logger_.info(String.format("to=%s", to));
		
		Random r = new Random();
		subject = String.format("test: %d", r.nextInt());
		System.out.format("subject: %s",subject);

	    /*
	     * Initialize the JavaMail Session.
	     */
	    Properties props = System.getProperties();
	    mailhost = host_;
	    if (mailhost != null)
		    props.put("mail.smtp.host", mailhost);
        props.put("mail.smtp.port", 587);

        props.put("mail.debug", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.user", user_);
        props.put("mail.smtp.password",password_);
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.EnableSSL.enable","true");
        password = password_;
        host = host_;
        user = user_;

        System.out.format("host=%s",mailhost);

	    // Get a Session object
        SmtpAuthenticator authentication = new SmtpAuthenticator(user_,password_);
	    Session session = Session.getInstance(props, authentication);
	    if (debug)
		    session.setDebug(true);

	    /*
	     * Construct the message and send it.
	     */
	    Message msg = new MimeMessage(session);
	    if(m != null) {
	    		msg.setReplyTo(m.getFrom());
	    		Message rm = m.reply(true);
	    		String subj = rm.getSubject();
	    		logger_.info(String.format("subj: %s", subj));
	    		msg.setSubject(subj);
	    }
	    if (from != null)
	    		msg.setFrom(new InternetAddress(from));
	    else
		msg.setFrom();

	    msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
	    if (cc != null)
		msg.setRecipients(Message.RecipientType.CC,
					InternetAddress.parse(cc, false));
	    if (bcc != null)
		msg.setRecipients(Message.RecipientType.BCC,
					InternetAddress.parse(bcc, false));

	    String text = string;

	    if (file != null) {
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setText(text);
		MimeBodyPart mbp2 = new MimeBodyPart();
		mbp2.attachFile(file);
		MimeMultipart mp = new MimeMultipart();
		mp.addBodyPart(mbp1);
		mp.addBodyPart(mbp2);
		msg.setContent(mp);
	    } else {
		// If the desired charset is known, you can use
		// setText(text, charset)
		msg.setText(text);
	    }

	    msg.setHeader("X-Mailer", mailer);
	    msg.setSentDate(new Date());

	    // send the thing off
	    Transport.send(msg);

	    System.out.println("\nMail was sent successfully.");

	    /*
	     * Save a copy of the message, if requested.
	     */
	    if (record != null) {
		// Get a Store object
		Store store = null;
		if (url != null) {
		    URLName urln = new URLName(url);
		    store = session.getStore(urln);
		    store.connect();
		} else {
		    if (protocol != null)		
			    store = session.getStore(protocol);
		    else
			    store = session.getStore();
			    store.connect();
		}

		// Get record Folder.  Create if it does not exist.
		Folder folder = store.getFolder(record);
		if (folder == null) {
		    System.err.println("Can't get record folder.");
		    System.exit(1);
		}
		if (!folder.exists())
		    folder.create(Folder.HOLDS_MESSAGES);

		Message[] msgs = new Message[1];
		msgs[0] = msg;
		folder.appendMessages(msgs);

		System.out.println("Mail was recorded successfully.");
    		    
    		}
    	    }
   }