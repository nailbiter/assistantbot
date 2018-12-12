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
//		System.err.format("%s executes %s\n", this.getClass().getName(),arg);
		for(ScriptHelper sh:helpers_) {
			try {
				res = sh.execute(arg);
			} catch(Exception e) {
//				System.err.format("was here with %s\n", sh.getClass().getName());
//				e.printStackTrace();
				continue;
			}
//			System.err.format("was here with %s and %s\n", sh.getClass().getName(),res);
			if( res != null )
				return res;
		}
		throw new AssistantBotException(AssistantBotException.Type.SCRIPTHELPERARRAY, String.format("noone could parse \"%s\"", arg));
	}

	@Override
	public void setInvocable(Invocable inv) {
	}
}
