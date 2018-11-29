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
	protected ParseOrdered po_ = null;
	protected AbstractManager(JSONArray commands) {
		po_ = new ParseOrdered(commands,this.getClass().getName());
	}
	/* (non-Javadoc)
	 * @see util.MyManager#getResultAndFormat(org.json.JSONObject)
	 */
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		System.err.println(String.format("%s got: %s",this.getClass().getName(), res.toString()));
//		if(res.has(CMD))
//		{
			System.err.println("dispatcher got: "+res.toString());
			return (String)this.getClass().getMethod(res.getString(CMD),JSONObject.class)
					.invoke(this,po_.parse(res));
//		}
//		return null;
	}
	protected JSONObject getParamObject(MongoClient mc) throws JSONException, Exception {
		return GetParamObject(mc,this.getClass().getName());
	}
	protected static JSONObject GetParamObject(MongoClient mc,String classname) throws JSONException, Exception {
		System.err.format("getting param object for %s\n", classname);
		JSONObject parameters = MongoUtil.GetJsonObjectFromDatabase(mc, 
				"logistics.params",
				"name:"+classname).getJSONObject("parameter");
		return parameters;
	}
	public JSONObject getCommands() {
		JSONObject res = po_.getCommands();
		System.err.format("getCommands for %s got %s\n", this.getClass().getName(),res);
		return res;
	}
}
