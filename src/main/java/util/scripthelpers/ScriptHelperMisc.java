package util.scripthelpers;

import javax.script.Invocable;

import org.json.JSONObject;

import util.Util;

public class ScriptHelperMisc implements ScriptHelper {
	public final static String CMD = "cmd";
	public final static String DATA = "data";
	@Override
	public String execute(String arg) throws Exception {
		JSONObject query = new JSONObject(arg);
		if( query.getString(CMD).equals("daysTill")) {
			return Double.toString(Util.DaysTill(query.getString(DATA)));
		} else {
			return null;
		}
	}

	@Override
	public void setInvocable(Invocable inv) {
	}

}
