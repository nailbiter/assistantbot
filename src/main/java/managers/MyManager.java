package managers;

import org.json.JSONObject;

public interface MyManager extends Replier{
	/**
	 * 
	 * @param res [res.cmd: command name] (if not given, means default handler), res.rem: remaining part 
	 * @return
	 * @throws Exception
	 */
	abstract public String getResultAndFormat(JSONObject res) throws Exception;
	abstract public JSONObject getCommands();
	public abstract void set() throws Exception;
}
