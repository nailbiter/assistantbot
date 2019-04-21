package managers.trello;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.json.JSONArray;

import com.github.nailbiter.util.TrelloAssistant;

import assistantbot.ResourceProvider;
import managers.AbstractManager;
import managers.habits.Constants;
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
		} else if(split.length==5) {
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
			for(Object o:cards) {
				JSONObject list = (JSONObject) o;
				if(list.getString("name").equals(split[3])){
					card=list;
					cards = list.getJSONArray("checkItems");
					break;
				}
			}
			for(int i = 0; i < cards.length(); i++) {
				if(cards.getJSONObject(i).getString("state").equals("complete")) {
					cards.remove(i);
					i--;
				}
			}
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
	protected void moveCard(JSONObject card, String destid) throws Exception {
		if(card.has("idChecklist")) {
//			System.err.format("cards: %s\n", ta_.getClosedCardsInBoard(Constants.BOARDIDS.INBOX.toString()).toString(2));
//			throw new Exception();
			if(TrelloAssistantChild.IsTrelloUrl(card.getString("name"))) {
				String[] boardsToCheck = new String[]{Constants.BOARDIDS.INBOX.toString(),Constants.BOARDIDS.HABITS.toString()};
				List<JSONObject> cards = new ArrayList<JSONObject>(); 
				for(String boardid:boardsToCheck) {
//					System.err.format("cards: %s\n", ta_.getClosedCardsInBoard(Constants.BOARDIDS.INBOX.toString()).toString(2));
					JSONArray res = ta_.getClosedCardsInBoard(boardid);
					for(Object o:res) {
						cards.add((JSONObject) o);
					}
				}
				for(JSONObject c:cards) {
					if(c.getString("shortUrl").equals(card.getString("name"))) {
						System.err.format("%s: found card %s => %s\n", "UrUq9T51DiGu",c.getString("shortUrl"),c.toString(2));
						ta_.moveCard(card.getString("id"), destid);
						return;
					}
				}
				System.err.format("%s: cound not find card %s\n", "UrUq9T51DiGu",card.getString("name"));
				return;
			} else {
				ta_.addCard(destid
						, new JSONObject()
							.put("name", card.getString("name")));
			}
		} else {
			String cardid = card.getString("id");
			System.err.format("moving %s to %s\n", cardid,destid);
			ta_.moveCard(cardid, destid);
		}
	}
}
