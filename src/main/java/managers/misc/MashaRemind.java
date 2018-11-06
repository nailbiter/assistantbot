package managers.misc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;
import com.github.nailbiter.util.Util;

import util.TableBuilder;

public class MashaRemind {
	private static final String MASHALISTID = "5b610f6d34d70854e769326d";
	private static final SimpleDateFormat DF = Util.GetTrelloDateFormat();
	private static final String BASICMESSAGE = "Маша, у тебя есть несделанные задачи:";
	public static String Remind(TrelloAssistant ta) throws Exception {
		JSONArray array = ta.getCardsInList(MASHALISTID);
		StringBuilder sb = new StringBuilder();
		ArrayList<JSONObject> tasks = new ArrayList<JSONObject>();
		for(Object o:array) {
			JSONObject obj = (JSONObject)o;
			if(obj.optBoolean("dueComplete",false)==false && !obj.isNull("due")) {
				Date due = DF.parse(obj.getString("due")),
						now = new Date();
				JSONObject oo = new JSONObject()
						.put("name", obj.getString("name"))
						.put("daysTill", (due.getTime()-now.getTime())/(1000*60*60*24*1.0d));
//				sb.append(oo.toString()+"\n");
				tasks.add(oo);
			}
		}
		Collections.sort(tasks, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				return -Double.compare(o1.getDouble("daysTill"), o2.getDouble("daysTill"));
			}
		});
		
		sb.append(BASICMESSAGE+"\n\n");
		TableBuilder tb = new TableBuilder();
		tb.addNewlineAndTokens("название", "осталось дней");
		for(JSONObject obj:tasks) {
			tb.newRow();
			tb.addToken(CutString(obj.getString("name"),8));
			if(obj.getDouble("daysTill")<1) {
				tb.addToken(String.format("%.3f", obj.getDouble("daysTill")));
			} else {
				tb.addToken(StringUtils.repeat("*",(int)obj.getDouble("daysTill")));
			}
		}
		sb.append(tb.toString());
		
		return sb.toString();
	}
	static String CutString(String in,int maxlen) {
		if(in.length()<=maxlen) {
			return in;
		} else {
			return in.substring(0, maxlen)+"...";
		}
	}
}
