package util.scripthelpers;

import java.util.ArrayList;

import javax.script.Invocable;

import util.AssistantBotException;
import util.AssistantBotException.Type;

public class ScriptHelperArray implements ScriptHelper {
	ArrayList<ScriptHelper> helpers_ = new ArrayList<ScriptHelper>();
	public ScriptHelperArray add(ScriptHelper sh) {
		helpers_.add(sh);
		return this;
	}
	@Override
	public String execute(String arg) throws Exception {
		String res = null;
		for(ScriptHelper sh:helpers_) {
			try {
				res = sh.execute(arg);
			} catch(Exception e) {
				continue;
			}
			if( res != null ) {
				return res;
			}
		}
		throw new AssistantBotException(AssistantBotException.Type.SCRIPTHELPERARRAY
				, String.format("noone could parse \"%s\"", arg));
	}

	@Override
	public void setInvocable(Invocable inv) {
	}
}
