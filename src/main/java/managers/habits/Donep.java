package managers.habits;

import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;

import assistantbot.ResourceProvider;
import util.AssistantBotException;
import util.Message;
import util.parsers.FlagParser;

import static managers.habits.Constants.HABITBOARDID;
import static managers.habits.Constants.TODOLISTNAME;

/**
 * 
 * @author oleksiileontiev
 * class implementing the /donep functionality
 */
public class Donep {
	private TrelloAssistant ta_;
	private ResourceProvider rp_;
	private Hashtable<Integer, String> optionMsgs_;
	private Hashtable<String, Integer> names_ = new Hashtable<String,Integer>();
	private FlagParser fp_;
	private String name_;

	public Donep(TrelloAssistant ta, ResourceProvider rp, Hashtable<Integer, String> optionMsgs) throws AssistantBotException {
		ta_ = ta;
		rp_ = rp;
		optionMsgs_ = optionMsgs;
		fp_ = new FlagParser()
				.addFlag('s', "same")
				.addFlag('c', "choose");
	}
	private JSONArray getCards() throws Exception {
		JSONArray cards = ta_.getCardsInList(ta_.findListByName(HABITBOARDID, TODOLISTNAME));
		names_.clear();
		for(Object o:cards) {
			String name = ((JSONObject)o).getString("name");
			if(!names_.containsKey(name))
				names_.put(name, 0);
			names_.put(name, 1+names_.get(name));
		}
		return cards;
	}
	private String donep() throws Exception {
		/*JSONArray cards = */getCards();
		JSONArray opts = new JSONArray();
		
		for(String name:names_.keySet())
			opts.put(String.format("%s: %d", name,names_.get(name)));
		int id = rp_.sendMessageWithKeyBoard(new Message("which habbit?"), opts);
		optionMsgs_.put(id,"donep");
		return "";
	}
	public String donep(String code) throws JSONException, Exception {
		return removeCard( name_ = code.substring(0, code.lastIndexOf(':')) );
	}
	private String removeCard(String name) throws JSONException, Exception {
		JSONArray cards = getCards();
		JSONObject obj = null;
		for(Object o:cards) {
			if(((JSONObject)o).getString("name").equals(name)) {
				obj = (JSONObject)o;
				break;
			}
		}
		if( obj != null ) {
			ta_.removeCard(obj.getString("id"));
			return String.format("removed \"%s\", %d remains", obj.getString("name"),names_.get(obj.getString("name"))-1);
		} else {
			return String.format("no card named \"%s\" found", name);
		}
	}
	public String donepFlags(String flags) throws JSONException, Exception {
		fp_.parse(flags);
		if(fp_.contains('c'))
			return donep();
		else if(fp_.contains('s'))
			return removeCard(name_);
		else
			return fp_.getHelp();
	}
}
