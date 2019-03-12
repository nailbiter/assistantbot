package managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;

import assistantbot.ResourceProvider;
import managers.habits.Constants;
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

public class NewTrelloManager extends WithSettingsManager{
	private TrelloAssistant ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
			KeyRing.getTrello().getString("token"));
	private Hashtable<String, ImmutablePair<String,Transformer<Object,String>>> dispatch_;
	public NewTrelloManager(ResourceProvider rp) throws Exception {
		super(GetCommands(),rp);
		ImmutablePair<String[],Object[]> namesAndObjects = GetNamesAndObjects(ta_);
		this.addSettingEnum("tasklist", namesAndObjects.left, namesAndObjects.right, 0);
		dispatch_ = FillDispatch(ta_,rp_); 
	}
	private static ImmutablePair<String[], Object[]> GetNamesAndObjects(TrelloAssistant ta) throws Exception {
		ArrayList<ImmutablePair<String,String>> pairs = 
				new ArrayList<ImmutablePair<String,String>> ();
		pairs.add(new ImmutablePair<String,String>(Constants.BOARDIDS.HABITS.toString()
						,Constants.LISTNAMES.todo.toString() ));
		pairs.add(new ImmutablePair<String,String>(
			Constants.BOARDIDS.DREAMPIRATES.toString()
			,Constants.LISTNAMES.TODOcode.toString() ));
		
		ArrayList<String> names = new ArrayList<String> ();
		ArrayList<Object> objects = new ArrayList<Object> ();
		for(ImmutablePair<String, String> pair:pairs) {
			names.add(String.format("%s/%s", pair.left,pair.right));
			objects.add(ta.findListByName(pair.left,pair.right));
		}
		
		return new ImmutablePair<String[], Object[]>(names.toArray(new String[] {})
				,objects.toArray(new Object[] {}));
	}
	private static Hashtable<String, ImmutablePair<String, Transformer<Object, String>>> FillDispatch(final TrelloAssistant ta, ResourceProvider rp) {
		Hashtable<String, ImmutablePair<String, Transformer<Object, String>>> res = 
				new Hashtable<String, ImmutablePair<String, Transformer<Object, String>>>();
		
		res.put("done", new ImmutablePair<String, Transformer<Object, String>>("archive task"
				,new Transformer<Object,String>(){
					@Override
					public String transform(Object arg0) {
						ImmutablePair<JSONObject,Integer> pair = (ImmutablePair<JSONObject, Integer>) arg0;
						JSONObject obj = pair.left;
						try {
							ta.archiveCard(obj.getString("id"));
						} catch (Exception e) {
							e.printStackTrace();
							Util.ExceptionToString(e);
						}
						return String.format("archived task \"%s\"%s", obj.getString("name")
								,(pair.right>1)?String.format("\n%d remains", pair.right-1):"");
					}
				}));
		
		String[] cats = getCats(rp);
		for(String cat:cats)
			res.put(cat, MoveToTodoAndPutLabel(cat,ta));
		
//		res.put("help", new ImmutablePair<String,Transformer<Object,String>>("show this help message"
//				,new Transformer<Object,String>(){
//					@Override
//					public String transform(Object input) {
//						TableBuilder tb = new TableBuilder();
//						tb.addTokens("name_","description_");
//						for(String t:res.keySet())
//							tb.addTokens(t,res.get(t).left);
//						return tb.toString();
//					}
//				}));
		
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
						ImmutablePair<JSONObject,Integer> obj = (ImmutablePair<JSONObject,Integer>)arg0;
						JSONObject card = obj.left;
						String oldlistid, newlistid;
						try {
							oldlistid = ta.findListByName(Constants.BOARDIDS.HABITS.toString()
									, Constants.LISTNAMES.todo.toString());
							newlistid = ta.findListByName(Constants.BOARDIDS.INBOX.toString()
									, "inbox");

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
						.useMemory()))
				;
	}
	public String ttask(JSONObject obj) throws Exception {
		HashMap<String, Object> parsed = new ParseCommentLine(ParseCommentLine.Mode.FROMRIGHT)
				.parse(obj.getString("task"));
		Set<String> tags = (Set<String>) parsed.get(ParseCommentLine.TAGS);
		if( tags.isEmpty() ) {
			ArrayList<String> res = new ArrayList<String>();
			JSONObject card = ta_.addCard(getTasklist_()
					, new JSONObject().put("name", parsed.get(ParseCommentLine.REM)));
			if( parsed.containsKey(ParseCommentLine.DATE) ) {
				Date d = (Date) parsed.get(ParseCommentLine.DATE);
				ta_.setCardDue(card.getString("id"), d);
				res.add(String.format("due %s", d));
			}
			
			JsonUtil.FilterJsonKeys(card, new JSONArray().put("name").put("shortUrl"));
			res.add(String.format("added task %s", card.toString(2)));
			return String.join("\n", res);
		} else if (tags.size()>1) {
			return String.format("cannot be more than one tag: %s", tags.toString());
		}
		
		String tag = tags.iterator().next();
		if(tag.equals("help")) {
			TableBuilder tb = new TableBuilder();
			tb.addTokens("name_","description_");
			for(String t:dispatch_.keySet())
				tb.addTokens(t,dispatch_.get(t).left);
			return tb.toString();
		} else if( !dispatch_.containsKey(tag) ) {
			return String.format("cannot process tag \"%s\"", tag);
		}
		
		JSONArray cards = ta_.getCardsInList(getTasklist_());
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
						,new ImmutablePair<JSONObject,Integer>(count.get(key).left,keycount));
			}
			rp_.sendMessageWithKeyBoard("which card?", map, dispatch_.get(tag).right);
			return "";
		} else {
			JSONObject card = ta_.addCard(getTasklist_(), new JSONObject().put("name", parsed.get(ParseCommentLine.REM)));
			JsonUtil.FilterJsonKeys(card, new JSONArray().put("name").put("shortUrl"));
			return String.format("%s\nadded task %s with tag \"%s\"",dispatch_.get(tag).right.transform(card), card.toString(2),tag);
		}
	}
	private String getTasklist_() throws JSONException, Exception {
		return (String) this.getSetting("tasklist");
	}
}
