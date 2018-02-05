package mail;

import javax.mail.Message;

//FIXME: Message m -> JSONObject m (decouple)
public interface MailReplier {
	public void onMessageArrived(Message m) throws Exception;
}