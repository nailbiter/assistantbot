package managers.trello;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.json.JSONArray;

import com.github.nailbiter.util.TrelloAssistant;

import assistantbot.ResourceProvider;
import managers.AbstractManager;
import util.KeyRing;
import org.json.JSONObject;

public class TrelloManagerBase extends AbstractManager {

	protected TrelloAssistantChild ta_;

	protected TrelloManagerBase(JSONArray commands,ResourceProvider rp) {
		super(commands);
		ta_ = new TrelloAssistantChild(KeyRing.getTrello().getString("key"),
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
	protected static String GetListId(TrelloAssistant ta_, String src) throws Exception {
		String[] split = src.split("/");
		return ta_.findListByName(ta_.findBoardByName(split[0]), split[1]);
	}
	protected static List<JSONObject> GetCardList(String src,TrelloAssistantChild ta) throws Exception {
		JSONArray cards = new JSONArray();
		String[] split = src.split("/");
		if(split.length==3) {
			cards = ta.getCardsInList(GetListId(ta,split[0]+"/"+split[1]));
		} else if(split.length==4) {
			JSONArray cardsInList = ta.getCardsInList(GetListId(ta,split[0]+"/"+split[1]));
			JSONObject card = null;
			for(Object o:cardsInList) {
				if(((JSONObject)o).getString("name").equals(split[2])) {
					card = (JSONObject) o;
					break;
				}
			}
			cards = ta.getChecklistsOfCard(card.getString("id"));
			System.err.format("checklists: %s\n", cards.toString(2));
//			return cards.toString(2);
		}
		
		ArrayList<JSONObject> res = new ArrayList<JSONObject>();
		Predicate<JSONObject> predicate = MakeFilter(split[split.length-1]);
		for(Object o:cards) {
			JSONObject card = (JSONObject) o;
			if(predicate.test(card)) {
				res.add(card);
			}
		}
		return res;
	}
}
