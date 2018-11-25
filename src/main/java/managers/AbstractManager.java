/**
 * 
 */
package managers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.MongoClient;

import util.MongoUtil;
import util.parsers.ParseOrdered;
import static util.parsers.StandardParserInterpreter.CMD;

/**
 * @author nailbiter
 *
 */
public abstract class AbstractManager implements MyManager {
	ParseOrdered po_ = null;
	protected AbstractManager(JSONArray commands) {
		po_ = new ParseOrdered(commands);
	}
	/* (non-Javadoc)
	 * @see util.MyManager#getResultAndFormat(org.json.JSONObject)
	 */
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		System.err.println(String.format("%s got: %s",this.getClass().getName(), res.toString()));
		if(res.has(CMD) && hasCommand(res))
		{
			System.err.println("dispatcher got: "+res.toString());
			return (String)this.getClass().getMethod(res.getString(CMD),JSONObject.class)
					.invoke(this,po_.parse(res));
		}
		return null;
	}
	protected boolean hasCommand(JSONObject res) {
		return getCommands().keySet().contains(res.getString(CMD));
	}
	protected JSONObject getParamObject(MongoClient mc) throws JSONException, Exception {
		String classname = this.getClass().getName();
		System.err.format("getting param object for %s\n", classname);
		JSONObject parameters = MongoUtil.GetJsonObjectFromDatabase(mc, 
				"logistics.params",
				"name:"+classname).getJSONObject("parameter");
		return parameters;
	}
	public JSONObject getCommands() {
		return po_.getCommands();
	}
}
