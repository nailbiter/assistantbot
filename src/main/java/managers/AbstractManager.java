/**
 * 
 */
package managers;

import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public class AbstractManager implements MyManager {
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
			Object parsed = po_.parse(res);
			Class<? extends AbstractManager> classInstance = this.getClass();
			
			if( parsed instanceof JSONObject ) {
				return (String) classInstance
						.getMethod(res.getString(CMD),JSONObject.class)
						.invoke(this,parsed);
			} else if (parsed instanceof JSONArray ){
				return (String) classInstance
						.getMethod(res.getString(CMD),JSONArray.class)
						.invoke(this,parsed);
			}
			throw new Exception(String.format("unknown class \"%s\"", 
					parsed.getClass().getName()));
	}
	protected JSONObject getParamObject(ResourceProvider rp_) throws JSONException, Exception {
		return GetParamObject(rp_,getName());
	}
	protected String getName() {
		return this.getClass().getName();
	}
	public static JSONObject GetParamObject(ResourceProvider rp_,String classname) throws JSONException, Exception {
		return rp_.getManagerSettingsObject(classname);
	}
	public static String GetTimeZone(ResourceProvider rp) {
		return rp.getUserObject().getString("timezone");
	}
	public JSONObject getCommands() {
		JSONObject res = po_.getCommands();
		System.err.format("getCommands for %s got %s\n", this.getClass().getName(),res);
		return res;
	}
	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}
	@Override
	public void set() {
		
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
}
