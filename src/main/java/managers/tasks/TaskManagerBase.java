package managers.tasks;

import static managers.habits.Constants.SEPARATOR;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;
import com.github.nailbiter.util.Util;
import com.mongodb.MongoClient;

import assistantbot.ResourceProvider;
import managers.AbstractManager;
import util.KeyRing;

public class TaskManagerBase extends AbstractManager  {

	protected static final String POSTPONEDTASKS = "postponedTasks";
	protected Timer timer = new Timer();
	protected ResourceProvider rp_;
	protected TrelloAssistant ta_;
	protected MongoClient mc_;
	protected static int REMINDBEFOREMIN = 10;
	protected static String TASKNAMELENLIMIT = "TASKNAMELENLIMIT";
	protected static String INBOX = "INBOX";
	protected static String SNOOZED = "SNOOZED";
	protected static String SHORTURL = "shortUrl";
	protected HashMap<String,ImmutableTriple<Comparator<JSONObject>,String,Integer>> comparators_ = new HashMap<String,ImmutableTriple<Comparator<JSONObject>,String,Integer>>();

	protected static void FillTable(HashMap<String, ImmutableTriple<Comparator<JSONObject>, String, Integer>> c, TrelloAssistant ta) throws Exception {
		String listid = ta.findListByName(managers.habits.Constants.INBOXBOARDID, 
				managers.habits.Constants.INBOXLISTNAME);
		c.put(INBOX, new ImmutableTriple<Comparator<JSONObject>,String,Integer>(
				new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						return o1.getString("name")
								.compareTo(o2.getString("name"));
					}
				},
				listid, 1));
		c.put(SNOOZED, new ImmutableTriple<Comparator<JSONObject>,String,Integer>(
				new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						return o1.getString("name")
								.compareTo(o2.getString("name"));
					}
				},
				listid, 0));
		
	}

	protected static String PrintTasks(ArrayList<JSONObject> arr, int TNL) throws JSONException, ParseException {
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
			tb.addToken(card.getString("name"),TNL);
			tb.addToken(GetLabels(card),TNL);
			if(HasDue(card)) {
				tb.addToken(util.Util.PrintDaysTill(DaysTill(card), "="),TNL);
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

	protected static String PrintTask(ArrayList<JSONObject> arr, int index) {
		return String.format("%s %s",
				arr.get(index-1).getString("name"),
				arr.get(index-1).getString("shortUrl"));
	}

	private static String GetLabels(JSONObject card) {
		JSONArray label = card.optJSONArray("labels");
		if(label==null)
			return "";
		StringBuilder sb = new StringBuilder();
		for(Object o:label) {
			JSONObject obj = (JSONObject)o;
			if(obj.has("name"))
				sb.append(String.format("#%s, ", obj.getString("name")));
		}
		return sb.toString();
	}

	protected TaskManagerBase(JSONArray commands, ResourceProvider rp) throws Exception {
		super(commands);
		ta_ = new TrelloAssistant(KeyRing.getTrello().getString("key"),
				KeyRing.getTrello().getString("token"));
		rp_ = rp;
		mc_ = rp.getMongoClient();
		
		FillTable(comparators_,ta_);
	}

	protected Date ComputePostponeDate(String string) throws Exception {
		Matcher m = null;
		Calendar c = Calendar.getInstance();
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
		Collections.sort(res, triple.left);
		return res;
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
		.insertOne(Document.parse(new JSONObject()
				.put("date",new Date())
				.put("message",msg)
				.put("obj",obj)
				.toString()));
	}

}
