package managers.tasks;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static managers.habits.Constants.SEPARATOR;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Timer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;
import com.github.nailbiter.util.Util;
import com.mongodb.Block;
import com.mongodb.MongoClient;

import assistantbot.ResourceProvider;
import managers.AbstractManager;
import managers.TaskManager;
import util.JsonUtil;
import util.KeyRing;
import util.MongoUtil;
import util.ScriptApp;
import util.ScriptHelper;
import util.ScriptHelperArray;
import util.ScriptHelperLogger;
import util.ScriptHelperVarkeeper;

public class TaskManagerBase extends AbstractManager {

	protected static final String POSTPONEDTASKS = "postponedTasks";
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

	protected static String PrintTasks(ArrayList<JSONObject> arr, JSONObject po) throws JSONException, ParseException {
		TableBuilder tb = new TableBuilder();
		tb.newRow();
		tb.addToken("#_");
		tb.addToken("name_");
		tb.addToken("labels_");
		tb.addToken("due_");
		for(int i = 0;i < arr.size(); i++) {
			JSONObject card = arr.get(i);
			tb.newRow();
			tb.addToken(i + 1);
			tb.addToken(card.getString("name"),po.getJSONObject("sep").getInt("name"));
			tb.addToken(GetLabels(card),po.getJSONObject("sep").getInt("labels"));
			if(HasDue(card)) {
				tb.addToken(util.Util.PrintDaysTill(DaysTill(card), "="),po.getJSONObject("sep").getInt("due"));
			} else {
				tb.addToken("∞");
			}
		}
		return tb.toString();
	}

	private static double DaysTill(JSONObject obj) throws JSONException, ParseException {
		SimpleDateFormat DF = Util.GetTrelloDateFormat();
		Date due = DF.parse(obj.getString("due")),
				now = new Date();
		return (due.getTime()-now.getTime())/(1000*60*60*24*1.0d);
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

	private static String GetLabels(JSONObject card) {
		return GetLabels(card,new ArrayList<String>());
	}
	private static String GetLabels(JSONObject card,ArrayList<String> filter) {
		JSONArray label = card.optJSONArray("labels");
		if(label==null)
			return "";
		ArrayList<String> res = new ArrayList<String>();
		for(Object o:label) {
			JSONObject obj = (JSONObject)o;
			if(obj.has("name") && (filter.isEmpty() || filter.contains(obj.getString("name"))))
				res.add(String.format("#%s", obj.getString("name")));
		}
		return String.join(", ", res);
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
		final JSONObject po = GetParamObject(mc, TaskManager.class.getName()).getJSONObject("sep");
		final JSONArray alltasks = ta.getAllCardsInList(c.get(INBOX).middle);
		System.err.format("alltasks has %d cards\n", alltasks.length());
		final TableBuilder tb = new TableBuilder();
		tb.newRow().addToken("name_").addToken("label_");
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date d = cal.getTime();
		System.err.format("date: %s\n", d.toString());
		
		mc.getDatabase("logistics").getCollection("taskLog")
		.find(and(eq("message","taskdone"),gte("date",d)))
		.forEach(new Block<Document>() {
					@Override
					public void apply(Document arg0) {
						JSONObject obj = new JSONObject(arg0.toJson());
						System.err.format("obj=%s\n", obj.toString());
						JSONObject card = obj.getJSONObject("obj");
						tb.newRow()
						.addToken(card.getString("name"),po.getInt("name"))
						.addToken(GetLabels(card, recognizedCats),po.getInt("labels"));
					}
				});
		
		return tb.toString();
	}
	protected Date ComputePostponeDate(String string) throws Exception {
		Matcher m = null;
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("JST"));
		if((m = Pattern.compile("(\\d{2})(\\d{2})(\\d{2})(\\d{2})").matcher(string)).matches()) {
			c.set(Calendar.MONTH, Integer.parseInt(m.group(1))-1);
			c.set(Calendar.DATE, Integer.parseInt(m.group(2)));
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(3)));
			c.set(Calendar.MINUTE, Integer.parseInt(m.group(4)));
			return c.getTime();
		} if((m = Pattern.compile("(\\d{2})(\\d{2})").matcher(string)).matches()) {
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(1)));
			c.set(Calendar.MINUTE, Integer.parseInt(m.group(2)));
			return c.getTime();
		} else {
			throw new Exception(String.format("cannot parse %s", string));
		}
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
