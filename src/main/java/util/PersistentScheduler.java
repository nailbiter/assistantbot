/**
 * 
 */
package util;

import java.util.Timer;

import org.json.JSONObject;

/**
 * @author nailbiter
 *
 */
public abstract class PersistentScheduler {
	public PersistentScheduler(String filenameKey, String key, Timer timer,JSONObject schedulePattern)
	{
		//TODO
	}
	abstract protected void callback(int index);
	public void reset()
	{
		//TODO
	}
}
