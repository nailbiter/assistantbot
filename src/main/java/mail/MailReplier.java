package mail;

import org.json.JSONObject;

public interface MailReplier {
	public void onMessageArrived(JSONObject message) throws Exception;
}