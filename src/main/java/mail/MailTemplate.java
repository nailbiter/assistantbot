package mail;

import java.io.FileWriter;
import java.util.logging.Logger;

import util.LocalUtil;
import util.StorageManager;

public class MailTemplate {
	String content_ = null;
	protected static Logger logger_ = Logger.getLogger(MailTemplate.class.getName());
	public MailTemplate(String handle) throws Exception{
		logger_.info(String.format("handle: ", handle));
		content_ = StorageManager.getFile(handle);
	}
	@Override
	/** print body of the message
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return content_;
	}
}