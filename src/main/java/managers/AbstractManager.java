/**
 * 
 */
package managers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import managers.TaskManager.Task;
import util.MyManager;
import util.StorageManager;

/**
 * @author nailbiter
 *
 */
public abstract class AbstractManager implements MyManager {

	/* (non-Javadoc)
	 * @see util.MyManager#getResultAndFormat(org.json.JSONObject)
	 */
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		JSONArray cmds = this.getCommands();
		// TODO auto-dispatch
		return null;
	}
	protected JSONArray jsonarray = null;
	/* (non-Javadoc)
	 * @see util.MyManager#gotUpdate(java.lang.String)
	 */
	@Override
	public String gotUpdate(String data) throws Exception {
		return null;
	}
}
