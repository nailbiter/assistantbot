package managers.trello;

import com.github.nailbiter.util.TrelloAssistant;
import com.github.nailbiter.util.Util.HTTPMETHOD;

import static com.github.nailbiter.util.Util.HttpString;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TrelloAssistantChild extends TrelloAssistant {

	public TrelloAssistantChild(String key, String token) {
		super(key, token);
	}
	public JSONArray getChecklistsOfCard(String cardid) throws Exception {
		String uri = String.format("https://api.trello.com/1/cards/%s/checklists?%s"
				,cardid
				,JsonToUrl(getTokenObj())
		);
		String reply = HttpString(uri,client_,true,HTTPMETHOD.GET);
//		System.err.format("%s=>\n%s\n", itemName,reply);
		return new JSONArray(reply);
	}
}
