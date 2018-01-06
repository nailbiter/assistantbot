/**
 * 
 */
package managers;


import org.json.JSONObject;

import it.sauronsoftware.cron4j.Scheduler;
import util.MyBasicBot;
import util.MyManager;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
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
	}
	/* (non-Javadoc)
	 * @see util.MyManager#getResultAndFormat(org.json.JSONObject)
	 */
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("mail")==0)
				return this.checkMailBox();
		}
		return null;
	}
	protected String checkMailBox() throws Exception {
		StringBuilder sb = new StringBuilder();
		/*for(Folder f : fol_.list()){
			sb.append(f.getName()+"\n");
		}*/
		
		int count = 10;
		/*for(Message m : fol_.getMessages()){
			if(count==0) break;
			sb.append(String.format("%s - %s - %s\n", m.getSubject(), 
				m.getFrom()[0].toString(),
				m.getFlags().contains("Seen")));
			count--;
		}*/
		count = 10;
		sb.append(String.format("print only <=%d new now: \n",count));

		for(Message m : fol_.getMessages()){
			if(count==0) break;
			if(!isSeen(m.getFlags()))
			{
				sb.append(String.format("\t%s\n", //m.getSubject(), 
					m.getFrom()[0].toString()/*,
					m.getFlags().toString()*/));
				count--;
			}
		}
		return sb.toString();
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
}
