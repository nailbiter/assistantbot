package mail;

import javax.mail.Message;

import managers.Replier;

public class KMailReplier implements MailReplier, Replier {
	@Override
	public void onMessageArrived(Message m) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String processReply(int messageID, String msg) {
		// TODO Auto-generated method stub
		return null;
	}

}
