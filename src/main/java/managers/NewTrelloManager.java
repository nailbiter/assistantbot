package managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;

import assistantbot.ResourceProvider;
import util.AssistantBotException;
import util.JsonUtil;
import util.KeyRing;
import util.ParseCommentLine;
import util.UserCollection;
import util.Util;
import util.db.MongoUtil;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class NewTrelloManager extends AbstractManager {
	private static final String TASKLISTNAME = "todo";
	private TrelloAssistant ta_;
	private ResourceProvider rp_;
	private String tasklist_;
	private Hashtable<String, ImmutablePair<String,Transformer<Object,String>>> dispatch_;
	public NewTrelloManager(ResourceProvider rp) throws Exception {
		super(GetCommands());
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		tasklist_ = ta_.findListByName(managers.habits.Constants.HABITBOARDID, TASKLISTNAME);
		rp_ = rp;
		dispatch_ = FillDispatch(ta_,rp_); 
	}
	private static Hashtable<String, ImmutablePair<String, Transformer<Object, String>>> FillDispatch(final TrelloAssistant ta, ResourceProvider rp) {
		Hashtable<String, ImmutablePair<String, Transformer<Object, String>>> res = 
				new Hashtable<String, ImmutablePair<String, Transformer<Object, String>>>();
		
		res.put("done", new ImmutablePair<String, Transformer<Object, String>>("archive task"
				,new Transformer<Object,String>(){
					@Override
					public String transform(Object arg0) {
						JSONObject obj = (JSONObject) arg0;
						try {
							ta.archiveCard(obj.getString("id"));
						} catch (Exception e) {
							e.printStackTrace();
							Util.ExceptionToString(e);
						}
						return String.format("archived task \"%s\"", obj.getString("name"));
					}
				}));
		
		String[] cats = getCats(rp);
		for(String cat:cats)
			res.put(cat, MoveToTodoAndPutLabel(cat,ta));
		return res;
	}
	private static String[] getCats(ResourceProvider rp) {
		ArrayList<String> res = new ArrayList<String>();
		for(Object o:MongoUtil.GetJSONArrayFromDatabase(rp.getCollection(UserCollection.TIMECATS))) {
			res.add(((JSONObject)o).getString("name"));
		}
		return res.toArray(new String[] {});
	}
	private static ImmutablePair<String, Transformer<Object, String>> MoveToTodoAndPutLabel(final String cat, final TrelloAssistant ta) {
		return new ImmutablePair<String, Transformer<Object, String>>(cat,
				new Transformer<Object,String>(){
					@Override
					public String transform(Object arg0) {
						JSONObject card = (JSONObject) arg0;
						String oldlistid, newlistid;
						try {
							oldlistid = ta.findListByName(managers.habits.Constants.HABITBOARDID, "todo");
							newlistid = ta.findListByName(managers.habits.Constants.INBOXBOARDID, "inbox");

							System.err.format("old=%s\nnew=%s\n", oldlistid,newlistid);
							String cardid = card.getString("id");
							ta.moveCard(cardid, managers.habits.Constants.INBOXBOARDIDLONG+"."+newlistid,"bottom");
							
							ta.setLabelByName(cardid, cat,newlistid,TrelloAssistant.SetUnset.SET);
							
							return String.format("moved \"%s\"\n", 
									card.getString("name")
									);
						} catch (Exception e) {
							e.printStackTrace();
							return Util.ExceptionToString(e);
						}
					}
		});
	}
	private static JSONArray GetCommands() throws AssistantBotException {
		return new JSONArray()
				.put(new ParseOrderedCmd("ttask", "make new task", 
						new ParseOrderedArg("task",ParseOrdered.ArgTypes.remainder)
						.makeOpt()))
				;
	}
	public String ttask(JSONObject obj) throws Exception {
		if( !obj.has("task") ) {
			TableBuilder tb = new TableBuilder();
			tb.addTokens("name_","description_");
			for(String t:dispatch_.keySet())
				tb.addTokens(t,dispatch_.get(t).left);
			return tb.toString();
		}
		
		HashMap<String, Object> parsed = new ParseCommentLine(ParseCommentLine.Mode.FROMRIGHT)
				.parse(obj.getString("task"));
		Set<String> tags = (Set<String>) parsed.get(ParseCommentLine.TAGS);
		if( tags.isEmpty() ) {
			JSONObject card = ta_.addCard(tasklist_, new JSONObject().put("name", parsed.get(ParseCommentLine.REM)));
			JsonUtil.FilterJsonKeys(card, new JSONArray().put("name").put("shortUrl"));
			return String.format("added task %s", card.toString(2));
		} else if (tags.size()>1) {
			return String.format("cannot be more than one tag: %s", tags.toString());
		}
		
		String tag = tags.iterator().next();
		if( !dispatch_.containsKey(tag) ) {
			return String.format("cannot process tag \"%s\"", tag);
		}
		
		JSONArray cards = ta_.getCardsInList(tasklist_);
		if( !parsed.containsKey(ParseCommentLine.REM) ) {
			HashMap<String, ImmutablePair<JSONObject, MutableInt>> count = 
					new HashMap<String,ImmutablePair<JSONObject,MutableInt>>();
			for(Object o:cards) {
				JSONObject json = (JSONObject) o;
				String name = json.getString("name");
				if( count.containsKey(name) ) {
					count.get(name).right.increment();
				} else {
					count.put(name
							, new ImmutablePair<JSONObject,MutableInt>(json
									,new MutableInt(1)));
				}
			}
			HashMap<String, Object> map = new HashMap<String,Object>();
			for(String key:count.keySet()) {
				int keycount = count.get(key).right.intValue();
				map.put(String.format((keycount>1)?"%s:%d":"%s", key,keycount)
						,count.get(key).left);
			}
			rp_.sendMessageWithKeyBoard("which card?", map, dispatch_.get(tag).right);
			return "";
		} else {
			JSONObject card = ta_.addCard(tasklist_, new JSONObject().put("name", parsed.get(ParseCommentLine.REM)));
			JsonUtil.FilterJsonKeys(card, new JSONArray().put("name").put("shortUrl"));
			return String.format("%s\nadded task %s with tag \"%s\"",dispatch_.get(tag).right.transform(card), card.toString(2),tag);
		}
	}
}
