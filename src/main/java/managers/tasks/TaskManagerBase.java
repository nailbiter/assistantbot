package managers.tasks;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static managers.habits.Constants.SEPARATOR;
import static util.Util.PrintDaysTill;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Logger;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;

import assistantbot.ResourceProvider;
import managers.AbstractManager;
import managers.WithSettingsManager;
import util.AssistantBotException;
import util.JsonUtil;
import util.KeyRing;
import util.UserCollection;
import util.db.MongoUtil;
import util.parsers.FlagParser;
import util.parsers.ParseOrdered;
import util.scriptapps.JsApp;
import util.scriptapps.ScriptApp;
import util.scripthelpers.ScriptHelperArray;
import util.scripthelpers.ScriptHelperLogger;
import util.scripthelpers.ScriptHelperMisc;
import util.scripthelpers.ScriptHelperVarkeeper;

public class TaskManagerBase extends WithSettingsManager {

	private static final String LABELJOINER = ", ";
	private static final String MINDONE = "mindone";
	private static final String MAXDONE = "maxdone";
	private static final String INFTY = "∞";
	protected Timer timer = new Timer();
//	protected ResourceProvider rp_;
	protected TrelloAssistant ta_;
	private ScriptApp sa_;
	protected static int REMINDBEFOREMIN = 10;
	protected static String TASKNAMELENLIMIT = "TASKNAMELENLIMIT";
	protected static String INBOX = "INBOX";
	protected static String SNOOZED = "SNOOZED";
	protected static String SHORTURL = "shortUrl";
	protected HashMap<String,ImmutableTriple<Comparator<JSONObject>,String,Integer>> comparators_ = new HashMap<String,ImmutableTriple<Comparator<JSONObject>,String,Integer>>();
	private static ScriptHelperVarkeeper varkeeper_ = null;
	/**
	 * @deprecated
	 */
	protected ArrayList<String> recognizedCatNames_ = new ArrayList<String>();
	protected JSONArray cats_ = new JSONArray();
	protected FlagParser fp_;
	protected static final Map<String,Predicate<JSONObject>> TASKSVIEWSPECIALTAGS_ = CreateTaskViewSpecialTags();
	private static final String MAXSIZE = "maxsize";
	private static final int MAXSIZEINITVALUE = 32;

	protected TaskManagerBase(JSONArray commands, ResourceProvider rp) throws Exception {
		super(commands,rp);
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		addSettingScalar(MAXSIZE, ParseOrdered.ArgTypes.integer, MAXSIZEINITVALUE);
		varkeeper_ = new ScriptHelperVarkeeper();
		sa_ = new JsApp(getParamObject(rp).getString("scriptFolder"), 
				new ScriptHelperArray()
					.add(new ScriptHelperLogger())
					.add(new ScriptHelperMisc())
					.add(varkeeper_));
		FillTable(comparators_,ta_,sa_);
		FillRecognizedCats(recognizedCatNames_,rp,varkeeper_,cats_);
		
		fp_ = new FlagParser()
			.addFlag('a', "archive task")
			.addFlag('d', "done task")
			;
	}
	private static Map<String, Predicate<JSONObject>> CreateTaskViewSpecialTags() {
		Hashtable<String, Predicate<JSONObject>> res = new Hashtable<String, Predicate<JSONObject>>();
		res.put("overdue", new Predicate<JSONObject>() {
			@Override
			public boolean evaluate(JSONObject card) {
				try {
					return HasDue(card)
							&& ( DaysTill(card) < 0 )
							;
				} catch (JSONException | ParseException e) {
					e.printStackTrace();
					return false;
				}
			}
		});
		res.put("tomorrow", new Predicate<JSONObject>() {
			@Override
			public boolean evaluate(JSONObject card) {
				try {
					if( !HasDue(card) )
						return false;
					String string = card.getString("due");
					SimpleDateFormat DF = com.github.nailbiter.util.Util.GetTrelloDateFormat();
					Calendar due = Calendar.getInstance()
							, now = Calendar.getInstance();
					due.setTime(DF.parse(string));
					now.add(Calendar.DATE,1);
					return CompareCalendars(due,now,new Integer[] {
						Calendar.YEAR, Calendar.MONTH, Calendar.DATE	
					});
				} catch (JSONException | ParseException e) {
					e.printStackTrace();
					return false;
				}
			}
		});
		res.put("today", new Predicate<JSONObject>() {
			@Override
			public boolean evaluate(JSONObject card) {
				try {
					if( !HasDue(card) )
						return false;
					String string = card.getString("due");
					SimpleDateFormat DF = com.github.nailbiter.util.Util.GetTrelloDateFormat();
					Calendar due = Calendar.getInstance()
							, now = Calendar.getInstance();
					due.setTime(DF.parse(string));
					return CompareCalendars(due,now,new Integer[] {
						Calendar.YEAR, Calendar.MONTH, Calendar.DATE	
					});
				} catch (JSONException | ParseException e) {
					e.printStackTrace();
					return false;
				}
			}
		});
		
		return res;
	}
	private static boolean CompareCalendars(Calendar c1, Calendar c2, Integer[] fields) {
		for(int field:fields) {
			if( c1.get(field) != c2.get(field) ) {
				return false;
			}
		}
		return true;
	}
	private static void FillRecognizedCats(final ArrayList<String> recognizedCats,ResourceProvider rp, ScriptHelperVarkeeper varkeeper, final JSONArray cats){
		rp.getCollection(UserCollection.TIMECATS).find().forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				JSONObject obj = new JSONObject(arg0.toJson());
				recognizedCats.add(obj.getString("name"));
				cats.put(obj);
			}
		});
		varkeeper.set("recognizedCats", new JSONArray(recognizedCats).toString());
	}
	private static void FillTable(HashMap<String,ImmutableTriple<Comparator<JSONObject>,String,Integer>> c,TrelloAssistant ta,final ScriptApp sa) throws Exception {
		String listid = ta.findListByName(managers.habits.Constants.INBOXBOARDID, 
				managers.habits.Constants.INBOXLISTNAME);
		c.put(INBOX, new ImmutableTriple<Comparator<JSONObject>,String,Integer>(
				new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						try {
							int res = 
									Integer.parseInt(sa.runCommand(String.format("%s %s %s", "inbox",o1.getString("id"),o2.getString("id"))));
							System.err.format("comparing \"%s\" and \"%s\" gave %d\n", o1.getString("name"),o2.getString("name"),res);
							return res;
						} catch (Exception e) {
							e.printStackTrace();
							return 0;
						}
					}
				},listid,1));
		c.put(SNOOZED, new ImmutableTriple<Comparator<JSONObject>,String,Integer>(
				new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						try {
							return Integer.parseInt(sa.runCommand(String.format("%s %s %s", "snoozed",o1.getString("id"),o2.getString("id"))));
						} catch (Exception e) {
							e.printStackTrace();
							return 0;
						}
					}
				}
				,listid, 0));
		
	}

	protected static String PrintTasks(ArrayList<JSONObject> arr, JSONObject paramObj, ArrayList<String> recognizedCats, ArrayList<Predicate<JSONObject>> filters) throws JSONException, ParseException, AssistantBotException {
		System.err.format("PrintTasks: paramObj=%s\n", paramObj.toString(2));
		TableBuilder tb = new TableBuilder()
			.addTokens("#_","name_","labels_","due_");
		
		AssistantBotException isBad = null;
		
		boolean wasCut = false;
		final int MAXSIZEVAL = filters.isEmpty()? paramObj.getInt(MAXSIZE): -1;
		int size=arr.size();
		if( ( size > MAXSIZEVAL ) && ( MAXSIZEVAL >= 0 ) ) {
			wasCut = true;
			arr = util.Util.GetArrayHead(arr,MAXSIZEVAL);
		}
		
		for(int i = 0;i < arr.size(); i++) {
			JSONObject card = arr.get(i);
			
			boolean toContinue = false;
			for(Predicate<JSONObject> filter:filters) {
				if( !filter.evaluate(card) ) {
					toContinue = true;
					break;
				}
			}
			if( toContinue ) {
				continue;
			}
			
			tb.newRow()
			.addToken(i + 1)
			.addToken(card.getString("name"),paramObj.getJSONObject("sep").getInt("name"));
			
			HashSet<String> labelset = GetLabels(card.getJSONArray("labels")) ;
			for(String spectag:TASKSVIEWSPECIALTAGS_.keySet()) {
				if( labelset.contains(spectag) ) {
					throw new AssistantBotException(AssistantBotException.Type.TASKMANAGERBASE
							,String.format("card %s contains spec tag \"%s\"", card.toString(2),spectag));
				}
			}
			String mainLabel = null;
			try {
				mainLabel = GetMainLabel(labelset,recognizedCats);
			} catch (AssistantBotException e) {
				if( e.getType() == AssistantBotException.Type.NOTONEMAINLABEL )
					isBad = e;
				else
					throw e;
			}
			
			if( mainLabel != null ) {
				labelset.remove(mainLabel);
				tb.addToken(String.format("%s%s%s"
						, mainLabel
						,LABELJOINER
						,String.join(LABELJOINER, labelset))
					,paramObj.getJSONObject("sep").getInt("labels"));
			} else {
				tb.addToken(String.join(LABELJOINER, labelset)
						,paramObj.getJSONObject("sep").getInt("labels"));
			}
			if( HasDue(card) ) {
				tb.addToken(PrintDaysTill(DaysTill(card), "=",-1)
						,paramObj.getJSONObject("sep").getInt("due"));
			} else {
				tb.addToken(INFTY);
			}
		}
		
		StringBuilder sb = new StringBuilder(tb.toString());
		if( wasCut ) {
			sb.append(String.format("...%d\n", size));
		}
		if( isBad != null ) {
			sb.append(String.format("e: %s\n", isBad.getMessage()));
		}
		
		return sb.toString();
	}

	protected static String GetMainLabel(HashSet<String> labelset, ArrayList<String> recognizedCats) throws AssistantBotException {
		String res = null;
		for(String cat:recognizedCats) {
			String prefixedCat = 
					String.format("%s",cat);
			if(labelset.contains(prefixedCat)) {
				if(res!=null) {
					throw new AssistantBotException(AssistantBotException.Type.NOTONEMAINLABEL, String.format("%s -> %s", res,cat));
				} else {
					res = prefixedCat;
				}
			}
		}
		if( res == null)
			throw new AssistantBotException(AssistantBotException.Type.NOTONEMAINLABEL,labelset.toString());
		else
			return res;
	}
	private static double DaysTill(JSONObject obj) throws JSONException, ParseException {
		return util.Util.DaysTill(obj.getString("due"));
	}

	private static boolean HasDue(JSONObject card) {
		return card.optBoolean("dueComplete",false)==false && !card.isNull("due");
	}

	protected static String PrintTask(ArrayList<JSONObject> arr, int index, TrelloAssistant ta) throws JSONException, Exception {
		JSONObject card = arr.get(index-1);
		return String.format("%s %s"
				,card.getString("name")
				,card.getString("shortUrl")
				);
	}

	protected static HashSet<String> GetLabels(JSONArray labels) {
		HashSet<String> res = new HashSet<String>();
		if( labels == null )
			return res;
		for(Object o:labels) {
			JSONObject obj = (JSONObject)o;
			if( obj.has("name") )
				res.add(String.format("%s", obj.getString("name")));
		}
		return res;
	}
	protected static String PrintSnoozed(TrelloAssistant ta, ResourceProvider rp, 
			String listid, JSONObject po, Logger logger) throws Exception {
		JSONArray tasks = ta.getCardsInList(listid);
		MongoCollection<Document> coll = 
				rp.getCollection(UserCollection.POSTPONEDTASKS);
		JSONArray reminders = MongoUtil.GetJSONArrayFromDatabase(coll);
		
		Date now = new Date();
		ArrayList<JSONObject> res = new ArrayList<JSONObject>();
		for(Object o:reminders) {
			JSONObject obj = (JSONObject)o;
			Date d = util.db.MongoUtil.MongoDateStringToLocalDate(obj.getString("date"));
			if( d.after(now) ) {
				JSONObject habitObj = JsonUtil.FindInJSONArray(tasks, SHORTURL, obj.getString(SHORTURL));
				if(habitObj==null) {
					continue;
				} else {
					logger.warning(String.format("could not list %s\n", obj.toString(2)));
				}
				res.add(new JSONObject()
						.put("date", d)
						.put("name", habitObj.getString("name")));
			}
		}
		
		Collections.sort(res, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				Date d1 = (Date) o1.get("date"),
						d2 = (Date) o2.get("date");
				return d1.compareTo(d2);
			}
		});
		TableBuilder tb = new TableBuilder();
		tb.addNewlineAndTokens("#_", "name_", "date_");
		int i = 1;
		for(JSONObject obj:res) {
			tb.newRow();
			tb.addToken(i++);
			tb.addToken(obj.getString("name"),po.getJSONObject("sep").getInt("name"));
			tb.addToken(obj.get("date").toString(),po.getJSONObject("sep").getInt("date"));
		}
		
		return tb.toString();
	}

	protected static String PrintDoneTasks(HashMap<String,Integer> stat, ArrayList<AssistantBotException> exs, JSONArray cats) throws Exception {
		TableBuilder tb = new TableBuilder()
				.addTokens("cat_"
						,"min_"
						,"count_"
						,"max_"
						);
		int total = 0;
		boolean res = true;
		for(Object o:cats) {
			JSONObject cat = (JSONObject)o;
			Integer num = stat.getOrDefault(cat.getString("name"),0)
					,min = cat.optInt(MINDONE)
					,max = cat.optInt(MAXDONE)
					;
			if(max==0)
				continue;
			boolean localres = min<=num && (max<0 || num<=max);
			res = res && localres;
			tb
				.newRow()
				.addToken(String.format(localres?"%s":"[%s]", cat.getString("name")))
				.addToken(NegToInf(min))
				.addToken(num)
				.addToken(NegToInf(max))
				;
			total += num;
		}
		tb.addTokens("--","--","--","--");
		tb.newRow().addToken("TOTAL").addToken("").addToken(total);
		
		return tb.toString()
				+ (String.format("%s\n", res))
				+ (exs.isEmpty()?"":String.format("\ne: %s", exs.get(0).getMessage()));
	}
	private static String NegToInf(Integer min) {
		if( min < 0 ) {
			return INFTY;
		} else {
			return min.toString();
		}
	}
	protected static HashMap<String, Integer> GetDoneTasksStat(TrelloAssistant ta, ResourceProvider rp,
			HashMap<String, ImmutableTriple<Comparator<JSONObject>, String, Integer>> c,
			final ArrayList<String> recognizedCats, 
			final ArrayList<AssistantBotException> exs) throws Exception {
		final JSONArray alltasks = ta.getAllCardsInList(c.get(INBOX).middle);
		System.err.format("alltasks has %d cards\n", alltasks.length());
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date d = cal.getTime();
		System.err.format("date: %s\n", d.toString());
		
		final HashMap<String,Integer> stat = new HashMap<String,Integer>();
		rp.getCollection(UserCollection.TASKLOG)
		.find(and(eq("message","taskdone"),gte("date",d)))
		.forEach(new Block<Document>() {
					@Override
					public void apply(Document arg0) {
						JSONObject obj = new JSONObject(arg0.toJson());
						System.err.format("obj=%s\n", obj.toString());
						JSONObject card = obj.getJSONObject("obj");
						try {
							String mainCat = GetMainLabel(GetLabels(card.getJSONArray("labels")),
									recognizedCats);
							if( !stat.containsKey(mainCat) )
								stat.put(mainCat, 0);
							stat.put(mainCat, stat.get(mainCat)+1);
						} catch (AssistantBotException e) {
							if(e.getType()==AssistantBotException.Type.NOTONEMAINLABEL)
								exs.add(0, e);
							else
								e.printStackTrace();
						}
					}
				});
		return stat;
	}
//	@Override
//	public String processReply(int messageID, String msg) {
//		return null;
//	}

	protected ArrayList<JSONObject> getTasks(String identifier) throws Exception {
		if( !comparators_.containsKey(identifier) )
			throw new Exception(String.format("unknown key %s", identifier));
		ImmutableTriple<Comparator<JSONObject>, String, Integer> triple = 
				comparators_.get(identifier);
		TrelloMover tm = new TrelloMover(ta_,triple.middle,SEPARATOR); 
		ArrayList<JSONObject> res = tm.getCardsInSegment(triple.right);
		FillVarkeeper(res,varkeeper_);
		Collections.sort(res, triple.left);
		return res;
	}

	private static void FillVarkeeper(ArrayList<JSONObject> res, ScriptHelperVarkeeper varkeeper) {
		for(JSONObject o:res) {
			JSONObject obj = new JSONObject(o.toString());
			JSONArray labels = o.getJSONArray("labels");
			obj.put("labels", new JSONArray());
			for(Object oo:labels)
				obj.getJSONArray("labels").put(((JSONObject)oo).getString("name"));
			varkeeper.set(o.getString("id"), obj.toString());
		}
			
	}
	protected void saveSnoozeToDb(JSONObject card, Date date) {
		rp_.getCollection(UserCollection.POSTPONEDTASKS)
		.insertOne(Document.parse(new JSONObject()
				.put("date", date)
				.put("shortUrl", card.getString("shortUrl"))
				.toString()));
	}

	protected void logToDb(String msg, JSONObject obj) {
		rp_.getCollection(UserCollection.TASKLOG)
		.insertOne(new Document("date",new Date())
					.append("message",msg)
					.append("obj",Document.parse(obj.toString())));
	}
	protected JSONObject getTask(int num) throws Exception {
		JSONObject card = null;
		if( num > 0 )
			card = getTasks(INBOX).get(num-1);
		else
			card = getTasks(SNOOZED).get(-num-1);
		return card;
	}
	protected static ArrayList<Integer> ParseIntList(String s){
		ArrayList<Integer> res = new ArrayList<Integer>();
		for( String split:s.split(",") )
			res.add( Integer.parseInt(split) );
		return res;
	}
	protected static boolean CannotDoTask(JSONArray cats_, String mc, HashMap<String, Integer> stat) throws AssistantBotException {
		JSONObject cat = JsonUtil.FindInJSONArray(cats_, "name", mc);
		int a = stat.getOrDefault(mc, 0),
				b = cat.optInt("maxdone", 0);
		boolean res = a >= b && b >= 0;
		if ( res ) {
			throw new AssistantBotException(AssistantBotException.Type.CANNOTDOTASK,
					String.format("main cat: %s, %d >= %d", mc,a,b));
		}
		return res;
	}
}
