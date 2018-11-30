package util;

import javax.script.Invocable;

public interface ScriptHelper {
	public abstract String execute(String arg) throws Exception;
	public abstract void setInvocable(Invocable inv);
}
