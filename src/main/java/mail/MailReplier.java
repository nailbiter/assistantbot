package mail;

import org.json.JSONObject;

import managers.OptionReplier;
import managers.Replier;

public interface MailReplier extends Replier, OptionReplier{
	public void onMessageArrived(JSONObject message) throws Exception;
}	