package managers.trello;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.json.JSONArray;

import com.github.nailbiter.util.TrelloAssistant;

import assistantbot.ResourceProvider;
import managers.AbstractManager;
import util.KeyRing;
import org.json.JSONObject;

public class TrelloManagerBase extends AbstractManager {

	protected TrelloAssistant ta_;

	protected TrelloManagerBase(JSONArray commands,ResourceProvider rp) {
		super(commands);
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
	}
	protected static Predicate<JSONObject> MakeFilter(String form){
		Pattern pat = Pattern.compile(form);
		return new Predicate<JSONObject>() {
			@Override
			public boolean test(JSONObject arg0) {
				return pat.matcher(arg0.getString("name")).matches();
			}
		};
	}
	protected String GetListId(TrelloAssistant ta_, String src) throws Exception {
		String[] split = src.split("/");
		return ta_.findListByName(ta_.findBoardByName(split[0]), split[1]);
	}
}
