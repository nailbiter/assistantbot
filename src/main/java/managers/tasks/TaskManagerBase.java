package managers.tasks;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static managers.habits.Constants.SEPARATOR;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.logging.Logger;

import javax.script.ScriptException;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;
import com.mongodb.Block;
import com.mongodb.MongoClient;

import assistantbot.ResourceProvider;
import managers.AbstractManager;
import managers.TaskManager;
import util.AssistantBotException;
import util.JsonUtil;
import util.KeyRing;
import util.MongoUtil;
import util.ParseCommentLine;
import util.ScriptApp;
import util.scripthelpers.ScriptHelperArray;
import util.scripthelpers.ScriptHelperLogger;
import util.scripthelpers.ScriptHelperMisc;
import util.scripthelpers.ScriptHelperVarkeeper;
import static util.Util.PrintDaysTill;

public class TaskManagerBase extends AbstractManager {

	protected static final String POSTPONEDTASKS = "postponedTasks";
	private static final String LABELJOINER = ", ";;
	protected Timer timer = new Timer();
	protected ResourceProvider rp_;
	protected TrelloAssistant ta_;
	protected MongoClient mc_;
	private ScriptApp sa_;
	protected static int REMINDBEFOREMIN = 10;
	protected static String TASKNAMELENLIMIT = "TASKNAMELENLIMIT";
	protected static String INBOX = "INBOX";
	protected static String SNOOZED = "SNOOZED";
	protected static String SHORTURL = "shortUrl";
	protected HashMap<String,ImmutableTriple<Comparator<JSONObject>,String,Integer>> comparators_ = new HashMap<String,ImmutableTriple<Comparator<JSONObject>,String,Integer>>();
	private ScriptHelperVarkeeper varkeeper_ = null;
	protected ArrayList<String> recognizedCats_ = new ArrayList<String>();

	protected TaskManagerBase(JSONArray commands, ResourceProvider rp) throws Exception {
		super(commands);
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		rp_ = rp;
		mc_ = rp.getMongoClient();
		varkeeper_ = new ScriptHelperVarkeeper();
		sa_ = new ScriptApp(getParamObject(mc_).getString("scriptFolder"), 
				new ScriptHelperArray()
					.add(new ScriptHelperLogger())
					.add(new ScriptHelperMisc())
					.add(varkeeper_));
		FillTable(comparators_,ta_,sa_);
		FillRecognizedCats(recognizedCats_,mc_,varkeeper_);
	}
	private static void FillRecognizedCats(final ArrayList<String> recognizedCats,MongoClient mc, ScriptHelperVarkeeper varkeeper){
		mc.getDatabase("logistics").getCollection("timecats").find().forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				recognizedCats.add(arg0.getString("name"));
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
						} catch (NumberFormatException | FileNotFoundException | NoSuchMethodException
								| ScriptException e) {
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
						} catch (NumberFormatException | FileNotFoundException | NoSuchMethodException
								| ScriptException e) {
							e.printStackTrace();
							return 0;
						}
					}
				}
				,listid, 0));
		
	}

	protected static String PrintTasks(ArrayList<JSONObject> arr, JSONObject po, ArrayList<String> recognizedCats) throws JSONException, ParseException, AssistantBotException {
		TableBuilder tb = new TableBuilder()
			.addTokens("#_","name_","labels_","due_");
		AssistantBotException isBad = null;
		
		for(int i = 0;i < arr.size(); i++) {
			JSONObject card = arr.get(i);
			tb.newRow()
			.addToken(i + 1)
			.addToken(card.getString("name"),po.getJSONObject("sep").getInt("name"));
			
			HashSet<String> labelset = GetLabels(card.getJSONArray("labels")) ;
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
					,po.getJSONObject("sep").getInt("labels"));
			} else {
				tb.addToken(String.join(LABELJOINER, labelset)
						,po.getJSONObject("sep").getInt("labels"));
			}
			if( HasDue(card) ) {
				tb.addToken(PrintDaysTill(DaysTill(card), "="),po.getJSONObject("sep").getInt("due"));
			} else {
				tb.addToken("∞");
			}
		}
		
		StringBuilder sb = new StringBuilder(tb.toString());
		if( isBad != null ) {
			sb.append(String.format("\ne: %s", isBad.getMessage()));
		}
		
		return sb.toString();
	}

	private static String GetMainLabel(HashSet<String> labelset, ArrayList<String> recognizedCats) throws AssistantBotException {
		String res = null;
		for(String cat:recognizedCats) {
			String prefixedCat = 
					String.format("%s%s", ParseCommentLine.TAGSPREF,cat);
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

	private static HashSet<String> GetLabels(JSONArray label) {
		HashSet<String> res = new HashSet<String>();
		if( label == null )
			return res;
		for(Object o:label) {
			JSONObject obj = (JSONObject)o;
			if( obj.has("name") )
				res.add(String.format("#%s", obj.getString("name")));
		}
		return res;
	}
	protected static String PrintSnoozed(TrelloAssistant ta, MongoClient mc, String listid, JSONObject po, Logger logger) throws Exception {
		JSONArray tasks = ta.getCardsInList(listid);
		JSONArray reminders = 
				MongoUtil.GetJSONArrayFromDatabase(mc, "logistics", POSTPONEDTASKS);
		
		Date now = new Date();
		ArrayList<JSONObject> res = new ArrayList<JSONObject>();
		for(Object o:reminders) {
			JSONObject obj = (JSONObject)o;
			Date d = util.Util.MongoDateStringToLocalDate(obj.getString("date"));
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

	protected static String PrintDoneTasks(TrelloAssistant ta, MongoClient mc, HashMap<String, ImmutableTriple<Comparator<JSONObject>, String, Integer>> c, final ArrayList<String> recognizedCats) throws Exception {
		final JSONArray alltasks = ta.getAllCardsInList(c.get(INBOX).middle);
		System.err.format("alltasks has %d cards\n", alltasks.length());
		TableBuilder tb = new TableBuilder().addTokens("cat_","count_");
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date d = cal.getTime();
		System.err.format("date: %s\n", d.toString());
		
		final ArrayList<AssistantBotException> exs = new ArrayList<AssistantBotException>();
		final HashMap<String,Integer> stat = new HashMap<String,Integer>();
		mc.getDatabase("logistics").getCollection("taskLog")
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
		
		int total = 0;
		for(String cat:stat.keySet()) {
			tb.newRow().addToken(cat)
			.addToken(stat.get(cat));
			total += stat.get(cat);
		}
		tb.newRow().addToken("TOTAL").addToken(total);
		
		return tb.toString() + 
				(exs.isEmpty()?"":String.format("\ne: %s", exs.get(0).getMessage()));
	}
	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}

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
		mc_.getDatabase("logistics").getCollection(POSTPONEDTASKS)
		.insertOne(Document.parse(new JSONObject()
				.put("date", date)
				.put("shortUrl", card.getString("shortUrl"))
				.toString()));
	}

	protected void logToDb(String msg, JSONObject obj) {
		mc_.getDatabase("logistics").getCollection("taskLog")
		.insertOne(new Document("date",new Date())
					.append("message",msg)
					.append("obj",Document.parse(obj.toString())));
	}
}
