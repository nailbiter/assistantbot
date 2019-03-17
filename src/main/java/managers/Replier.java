package managers;

import util.Message;

/**
 * @deprecated
 * @author oleksiileontiev
 *
 */
public interface Replier {
	public Message processReply(int messageID,String msg);
}
