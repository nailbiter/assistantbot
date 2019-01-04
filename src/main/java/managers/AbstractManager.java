/**
 * 
 */
package managers;

import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import assistantbot.ResourceProvider;
import util.SettingCollection;
import util.db.MongoUtil;
import util.parsers.ParseOrdered;
import static util.parsers.StandardParserInterpreter.CMD;

/**
 * @author nailbiter
 *
 */
public abstract class AbstractManager implements MyManager {
	protected ParseOrdered po_ = null;
	protected Logger logger_;
	protected AbstractManager(JSONArray commands) {
		po_ = new ParseOrdered(commands,this.getClass().getName());
		logger_ = Logger.getLogger(this.getClass().getName());
	}
	/* (non-Javadoc)
	 * @see util.MyManager#getResultAndFormat(org.json.JSONObject)
	 */
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		System.err.println(String.format("%s got: %s",this.getClass().getName(), res.toString()));
			System.err.println("dispatcher got: "+res.toString());
			return (String)this.getClass().getMethod(res.getString(CMD),JSONObject.class)
					.invoke(this,po_.parse(res));
	}
	protected JSONObject getParamObject(ResourceProvider rp_) throws JSONException, Exception {
		return GetParamObject(rp_,this.getClass().getName());
	}
	public static JSONObject GetParamObject(ResourceProvider rp_,String classname) throws JSONException, Exception {
		System.err.format("getting param object for %s\n", classname);
		MongoCollection<Document> coll = 
				MongoUtil.GetSettingCollection(rp_, SettingCollection.PARAMS);
		JSONObject parameters = MongoUtil
				.GetJsonObjectFromDatabase(coll,"name", classname)
				.getJSONObject("parameter");
		return parameters;
	}
	public JSONObject getCommands() {
		JSONObject res = po_.getCommands();
		System.err.format("getCommands for %s got %s\n", this.getClass().getName(),res);
		return res;
	}
}
