package util;

import org.json.JSONObject;

public class TrelloUtil {

	public static boolean HasDue(JSONObject card) {
		return card.optBoolean("dueComplete",false)==false && !card.isNull("due");
	}

}
