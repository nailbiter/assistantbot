package util.scripthelpers;

import java.util.HashMap;

import javax.script.Invocable;

public class ScriptHelperVarkeeper implements ScriptHelper{
	
	private HashMap<String, String> vars_;

	public ScriptHelperVarkeeper() {
		vars_ = new HashMap<String,String>();
	}
	@Override
	public String execute(String arg) throws Exception {
		String res = vars_.get(arg);
		System.err.format("get(%s)=%s\n", arg,res);
		return res;
	}
	public ScriptHelperVarkeeper set(String key,String val) {
		System.err.format("set(%s)=%s\n", key,val);
		vars_.put(key, val);
		return this;
	}
	@Override
	public void setInvocable(Invocable inv) {
	}
	public ScriptHelperVarkeeper set(String key,Object obj) {
		return set(key,obj.toString());
	}
}
