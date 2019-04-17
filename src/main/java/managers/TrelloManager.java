package managers;

import static managers.habits.Constants.HABITBOARDID;
import static managers.habits.Constants.INBOXBOARDID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;

import assistantbot.ResourceProvider;
import managers.habits.Constants;
import managers.trello.TrelloManagerBase;
import util.AssistantBotException;
import util.parsers.ArithmeticExpressionParser;
import util.parsers.ParseCommentLine;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;
import util.parsers.StandardParserInterpreter;;

public class TrelloManager extends TrelloManagerBase{
	public TrelloManager(ResourceProvider rp) throws AssistantBotException {
		super(GetCommands(),rp);
	}
	private static JSONArray GetCommands() throws AssistantBotException {
		ArrayList<String> commands = new ArrayList<String>();
		JSONArray res = new JSONArray();
		
//		for(String cmd:commands) {
//			res.put(new ParseOrderedCmd(cmd,cmd));
//		}
//		commands.clear();
		
		commands.add("makearchived");
		commands.add("addcard");
		commands.add("movetoeasytasks");
//		commands.add("countcard");
//		commands.add("getactions");
//		commands.add("removecards");
		for(String cmd:commands) {
			res.put(new ParseOrderedCmd(cmd,cmd,
					new ParseOrderedArg("rem", ArgTypes.remainder)));
		}
		commands.clear();
		
		res.put(new ParseOrderedCmd("trellomv"
				,"move from card/list to card/list"
				,new ParseOrderedArg("src",ArgTypes.string)
				,new ParseOrderedArg("dest",ArgTypes.string).makeOpt()));
		
		return res;
	}
	public String trellomv(JSONObject arg) throws Exception {
		//trellomv habits/TODO/test habits/todo
		//trellomv inbox/inbox/.*
		//trellomv inbox/inbox/GFmisc/.*
		
		List<JSONObject> cards = GetCardList(arg.getString("src"),ta_);
		String destid = arg.has("dest") ? GetListId(ta_,arg.getString("dest")) : null;
		
		StringBuilder sb = new StringBuilder();
		for(JSONObject card:cards) {
			System.err.format("card: %s\n", card.toString(2));
			
			if( destid == null ) {
				sb.append(String.format("name: %s\n", card.getString("name")));
			} else {
				String cardid = card.getString("id");
				System.err.format("moving %s to %s\n", cardid,destid);
				ta_.moveCard(cardid, destid);
			}
			
		}
		if(destid!=null) {
			sb.append(String.format("%d cards moved", cards.size()));
		}
		
		return sb.toString();
	}
	public String rename(JSONObject arg) throws Exception {
		String rem = arg.optString("rem","");
		String[] split = rem.split(" ",3);
		String listId = ta_.findListByName(HABITBOARDID, "TODO");
		JSONArray array = ta_.getCardsInList(listId),
				matched = new JSONArray();
		
		String regex = split[0];
		
		int count = 0;
		Hashtable<String,Integer> counthash = new Hashtable<String,Integer>(); 
		for(Object o : array) {
			JSONObject obj = (JSONObject)o;
			String name = obj.getString("name");
			if(Pattern.matches(regex, name)) {
				matched.put(obj);
				if(!counthash.containsKey(name)) {
					counthash.put(name, 0);
				}
				counthash.put(name, counthash.get(name)+1);
				count++;
			}
		}
		
		TableBuilder tb = new TableBuilder();
		tb.addNewlineAndTokens("name", "count");
		for(String name : counthash.keySet()) {
			tb.newRow();
			tb.addToken(name);
			tb.addToken(counthash.get(name));
		}
		if(split.length>=3 && split[2].equals("-t")) {
			return String.format("%s\ngoing to rename %d (out of %d) cards to \"%s\"",
					tb.toString(),
					count,
					array.length(),
					split[1]);
		} else {
			for(Object o:matched) {
				JSONObject obj = (JSONObject)o;
				ta_.renameCard(obj.getString("id"), split[1]);
			}
			return String.format("renaming %d (out of %d) cards to \"%s\"",
					count,
					array.length(),
					split[1]);
		}
		
	}
	
	public String movetoeasytasks(JSONObject arg) throws Exception {
		String oldlistid = ta_.findListByName(HABITBOARDID, "todo"),
				newlistid = ta_.findListByName(INBOXBOARDID, "sweet tasks");
		System.err.format("old=%s\nnew=%s\n", oldlistid,newlistid);
		JSONArray arr = ta_.getCardsInList(oldlistid);
		String cardid = arr.getJSONObject(arr.length()-1).getString("id");
		ta_.moveCard(cardid, Constants.INBOXBOARDIDLONG+"."+newlistid,"top");
		
		if( arg.has( StandardParserInterpreter.REM ) ) {
			String rem = arg.optString(StandardParserInterpreter.REM).trim();
			HashMap<String, Object> parsed = 
					new ParseCommentLine(ParseCommentLine.Mode.FROMLEFT)
					.parse(rem);
			HashSet<String> tags = 
					(HashSet<String>) parsed.get(ParseCommentLine.TAGS);
			for(String tag:tags)
				ta_.setLabelByName(cardid, tag,newlistid, TrelloAssistant.SetUnset.SET);
		}
		
		
		return String.format("moved \"%s\"\n", 
				arr.getJSONObject(arr.length()-1).getString("name")
				);
	}
	public String getactions(String rem) throws Exception {
		String listId = ta_.findListByName(HABITBOARDID, "TODO");
		JSONObject obj = null;
		if(!(rem==null || rem.isEmpty())) {
			obj = new JSONObject(rem);
		}
		final JSONObject filter = obj;
		JSONArray arr = ta_.getListActions(listId,obj);
		if(obj!=null) {
			ArrayList<JSONObject> coll = new ArrayList<JSONObject>();
			for(int i = 0; i < arr.length(); i++) {
				coll.add(arr.getJSONObject(i));
			}
			for(final String key:filter.keySet()) {
				coll.removeIf(new Predicate<JSONObject>() {
					@Override
					public boolean test(JSONObject t) {
						return !(t.getString(key).startsWith(filter.getString(key)));
					}
				});
			}
			arr = new JSONArray(coll);
		}
		return String.format("got: %s\n", arr.toString(2));
	}
	public String countcard(String rem) throws Exception {
		String listId = ta_.findListByName(HABITBOARDID, "TODO");
		JSONArray array = ta_.getCardsInList(listId);
		
		String regex = rem;
		
		int count = 0;
		Hashtable<String,Integer> counthash = new Hashtable<String,Integer>(); 
		for(Object o : array) {
			JSONObject obj = (JSONObject)o;
			String name = obj.getString("name");
			if(Pattern.matches(regex, name)) {
				if(!counthash.containsKey(name)) {
					counthash.put(name, 0);
				}
				counthash.put(name, counthash.get(name)+1);
				count++;
			}
		}
		
		TableBuilder tb = new TableBuilder();
		tb.addNewlineAndTokens("name", "count");
		for(String name : counthash.keySet()) {
			tb.newRow();
			tb.addToken(name);
			tb.addToken(counthash.get(name));
		}
		return String.format("%scounted %d (out of %d) cards with name \"%s\"",
				tb.toString(),
				count,array.length(),regex);
	}
	public String removecards(String rem) throws Exception {
		String[] split = rem.split(" ",2);
		String listId = ta_.findListByName(HABITBOARDID, "TODO");
		int count = ArithmeticExpressionParser.SimpleEvalInt(split[0]);
		String name = split[1];
		JSONArray array = ta_.getCardsInList(listId);
		
		int i = 0;
		for(Object o : array) {
			JSONObject obj = (JSONObject)o;
			String cardName = obj.getString("name");
			if(cardName.equals(name)) {
				if(i >= count) break;
				ta_.removeCard(obj.getString("id"));
				System.out.format("removed %s\n", obj.getString("id"));
				i++;
			}
		}
		
		return String.format("removed \"%s\" %d times", name,i);
	}
	public String addcard(JSONObject arg) throws Exception {
		String rem = arg.optString("rem","");
		String[] split = rem.split(" ",2);
		String listId = ta_.findListByName(HABITBOARDID, "TODO");
		JSONObject obj = new JSONObject()
				.put("name", split[1])
				.put("count", ArithmeticExpressionParser.SimpleEvalInt(split[0])),
				clone = new JSONObject(obj.toString());
		
		clone.remove("count");
		for(int i = 0, count = obj.getInt("count"); i < count;i++) {
			ta_.addCard(listId, clone);
		}
		
		return String.format("added \"%s\" %d times", obj.toString(),obj.getInt("count"));
	}
	public String makearchived(JSONObject arg) throws Exception {
		String rem = arg.getString("rem");
		String listId = ta_.findListByName(HABITBOARDID, "todo");
		
		JSONObject res = ta_.addCard(listId, new JSONObject().put("name", rem));
		String[] split = res.getString("shortUrl").split("/");
		String id = split[4];
		System.err.format("id: %s\n", id);
		ta_.archiveCard(id);
		return String.format("%s", res.getString("shortUrl"));
	}
//	static void makeCardWithCheckList() throws Exception{
//    	String listid =  ta_.findListByName(HABITBOARDID,"PENDING");
//    	JSONObject res = ta_.addCard(listid, new JSONObject()
//    			.put("name", "testcard")
//    			.put("checklist", new JSONArray()
//    					.put("checkname")
//    					.put("one")
//    					.put("two")
//    					.put("three")));
//    	System.out.format("hi there with!\n %s\n",res.toString(2)); 
//    }
//	public App() {
//		ta_ = new TrelloAssistant(secret.getString("trellokey"), 
//				secret.getString("trellotoken"));
//		ScriptEngineManager mgr = new ScriptEngineManager();
//	    engine_ = mgr.getEngineByName("JavaScript");
//	}

//	static public void putlabel() throws Exception {
//    	JSONArray cards = ta_.getCardsInList(list.getId());
//    	for(Object o:cards) {
//    		JSONObject obj = (JSONObject)o;
//    		System.out.println(String.format("going to put label for card %s", obj.getString("name")));
//    		ta_.setLabel(obj.getString("id"),"green");
//    	}
//    }
//    static public void readlabels() throws ClientProtocolException, IOException {
//    	Map<String, String> labels = board.getLabelNames();
//		for(String key:labels.keySet()) {
//			System.out.println(String.format("\t\tlabel: %s -- %s", key,labels.get(key)));
//		}
//    }
//    static public void readcard() throws Exception {
//    	JSONArray cards = ta_.getCardsInList(list.getId());
//    	System.out.println("here go the cards");
//    	System.out.println(cards.length());
//    	for(Object o:cards) {
//    		JSONObject obj = (JSONObject)o;
//    		System.out.println(String.format("\t%s", obj.toString()));
//    	}
//    }
//    static public void writecard() throws Exception {
//    	System.out.println("write card");
//    	JSONArray cards = ta_.getCardsInList(list.getId());
//    	System.out.println(cards.length());
//    	for(Object o:cards) {
//    		JSONObject obj = (JSONObject)o;
//    		if(obj.getString("name").equals("java test")) {
//    			System.out.println(String.format("here with id %s", obj.getString("id")));
//    			ta_.setCardDuedone(obj.getString("id"), true);
//    			break;
//    		}
//    	}
//    }
//    static public void makecard2() throws Exception {
//    	String listid =  ta_.findListByName(HABITBOARDID,"TODO");
//    	ta_.addCard(listid, new JSONObject()
//    			.put("name", "testname")
//    			.put("due", new Date()));
//    }
//    static public void makecard() {
//		System.out.println("\t\there");
//		Card card = new Card();
//		card.setName("java test");
//		Calendar calendar = Calendar.getInstance(); 
//		calendar.add(Calendar.HOUR, 4);  
//		Date due = calendar.getTime();
//		System.out.println(String.format("date: %s", due.toString()));
//		card.setDue(due);
//		list.createCard(card);
//    }
//    static void uploadsmalltasklist() throws Exception {
//    	String listid =  ta_.findListByName(HABITBOARDID,"TODO");
//    	System.out.format("id :%s\n", listid);
//    	try (BufferedReader br = new BufferedReader(new FileReader(resFolder+"smalltodo.txt"))) {
//    	    String line;
//    	    while ((line = br.readLine()) != null) {
//    	    	ta_.addCard(listid, new JSONObject().put("name", line));
//    	    }
//    	}
//    }
//	@Override
//	public String processReply(int messageID, String msg) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
