package util;

import java.util.logging.Logger;

import javax.script.Invocable;

import org.json.JSONException;
import org.json.JSONObject;

public class ScriptHelperLogger implements ScriptHelper {
	private Logger logger_ = null;

	public ScriptHelperLogger() {
		logger_  = Logger.getLogger(this.getClass().getName());
	}
	@Override
	public String execute(String arg) throws Exception {
		JSONObject obj ;
		try {
			obj = new JSONObject(arg);
		} catch(JSONException e) {
			throw e;
		}
		
		if(obj.getString("cmd").equals("log")) {
			log(obj.getString("data"));
			return "";
		} else {
			return null;
		}
	}

	private void log(String string) {
//		logger_.info(String.format("%s: %s", this.getClass().getName(),string));
		logger_.info(string);
	}
	@Override
	public void setInvocable(Invocable inv) {
	}

}
