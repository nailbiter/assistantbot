package managers;

import org.json.JSONObject;

import util.Message;

public interface MyManager{
	/**
	 * 
	 * @param res [res.cmd: command name] (if not given, means default handler), res.rem: remaining part 
	 * @return
	 * @throws Exception
	 */
	abstract public Message getResultAndFormat(JSONObject res) throws Exception;
	abstract public JSONObject getCommands();
	public abstract void set() throws Exception;
	public abstract String getName();
}
