/**
 * 
 */
package managers;

import util.Message;

/**
 * @author nailbiter
 *
 */
public interface OptionReplier {
	Message optionReply(String option, Integer msgID) throws Exception;
}
