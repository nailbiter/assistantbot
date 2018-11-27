package managers.habits;

import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;

import assistantbot.ResourceProvider;

import static managers.habits.Constants.HABITBOARDID;
import static managers.habits.Constants.TODOLISTNAME;

/**
 * 
 * @author oleksiileontiev
 * class implementing the /donep functionality
 */
public class Donep {
	private TrelloAssistant ta_;
	private ResourceProvider ud_;
	private Hashtable<Integer, String> optionMsgs_;
	JSONArray cards_;
	private Hashtable<String, Integer> names_ = new Hashtable<String,Integer>();

	public Donep(TrelloAssistant ta, ResourceProvider ud, Hashtable<Integer, String> optionMsgs) {
		ta_ = ta;
		ud_ = ud;
		optionMsgs_ = optionMsgs;
	}
	public String donep() throws Exception {
		String listid = ta_.findListByName(HABITBOARDID, TODOLISTNAME);
		cards_ = ta_.getCardsInList(listid);
		names_.clear();
		for(Object o:cards_) {
			String name = ((JSONObject)o).getString("name");
			if(!names_.containsKey(name))
				names_.put(name, 0);
			names_.put(name, 1+names_.get(name));
		}
		
		JSONArray opts = new JSONArray();
		for(String name:names_.keySet())
			opts.put(String.format("%s: %d", name,names_.get(name)));
		int id = ud_.sendMessageWithKeyBoard("which habbit?", opts);
		optionMsgs_.put(id,"donep");
		return "";
	}
	public String donep(String code) throws JSONException, Exception {
		JSONObject obj = null;
		String name = code.substring(0, code.lastIndexOf(':'));
		for(Object o:cards_) {
			if(((JSONObject)o).getString("name").equals(name)) {
				obj = (JSONObject)o;
				break;
			}
		}
		ta_.removeCard(obj.getString("id"));
		return String.format("removed \"%s\", %d remains", obj.getString("name"),names_.get(obj.getString("name"))-1);
	}
}
