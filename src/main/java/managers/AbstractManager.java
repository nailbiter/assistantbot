/**
 * 
 */
package managers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import managers.TaskManager.Task;
import util.StorageManager;
import util.parsers.StandardParser;

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
		System.out.println(String.format("got: %s", res.toString()));
		if(res.has("name") && hasCommand(res))
		{
			System.out.println("dispatcher got: "+res.toString());
			return (String)this.getClass().getMethod(res.getString("name"),JSONObject.class)
					.invoke(this,res);
		}
		return null;
	}
	protected boolean hasCommand(JSONObject res) {
		JSONArray cmds = this.getCommands();
		for(int i = 0; i < cmds.length(); i++)
			if(cmds.getJSONObject(i).getString("name").equals(res.getString("name")))
				return true;
		return false;
	}
	protected JSONArray jsonarray = null;
	static JSONObject makeCommand(String name,String help,List<JSONObject> args)
	{
		JSONObject cmd = new JSONObject();
		cmd.put("name", name);
		cmd.put("help", (help==null)?"(null)":help);
		JSONArray array = new JSONArray();
		for(int i = 0; i < args.size(); i++)
			array.put(args.get(i));
		cmd.put("args", array);
		return cmd;
	}
	static JSONObject makeCommandArg(String name,StandardParser.ArgTypes type,boolean isOpt)
	{
		JSONObject arg = new JSONObject();
		
		arg.put("name", name);
		if(isOpt) arg.put("isOpt", isOpt);
		arg.put("type", type.toString());
		
		return arg;
	}
}
