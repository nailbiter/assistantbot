package managers.misc;

import com.github.nailbiter.util.TrelloAssistant;

public class MashaRemind {
	private static final String MASHALISTID = "5b610f6d34d70854e769326d";
	public static String Remind(TrelloAssistant ta) throws Exception {
		return String.format("listid=%s", MASHALISTID);
	}
}
