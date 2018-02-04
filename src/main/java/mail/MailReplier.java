package mail;

import javax.mail.Message;

public interface MailReplier {
	public void onMessageArrived(Message m) throws Exception;
}
