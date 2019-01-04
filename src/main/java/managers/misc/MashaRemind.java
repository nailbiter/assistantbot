package managers.misc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.github.nailbiter.util.TrelloAssistant;
import com.github.nailbiter.util.Util;
import com.mongodb.MongoClient;

import assistantbot.ResourceProvider;
import managers.AbstractManager;
import util.db.MongoUtil;

public class MashaRemind {
	private static final String MASHALISTID = "5b610f6d34d70854e769326d";
	private static final SimpleDateFormat DF = Util.GetTrelloDateFormat();
	private static final String BASICMESSAGE = "Маша, у тебя есть несделанные задачи:";
	public static String Remind(TrelloAssistant ta, ResourceProvider rp_) throws Exception {
		JSONArray array = ta.getCardsInList(MASHALISTID);
		StringBuilder sb = new StringBuilder();
		ArrayList<JSONObject> tasks = new ArrayList<JSONObject>();
		JSONObject parameters = 
				AbstractManager.GetParamObject(rp_, MashaRemind.class.getName());
//		JSONObject parameters = MongoUtil.GetJsonObjectFromDatabase(rp_, 
//				"logistics.params",
//				"name:"+).getJSONObject("parameter");
		for(Object o:array) {
			JSONObject obj = (JSONObject)o;
			if(obj.optBoolean("dueComplete",false)==false && !obj.isNull("due")) {
				Date due = DF.parse(obj.getString("due")),
						now = new Date();
				JSONObject oo = new JSONObject()
						.put("name", obj.getString("name"))
						.put("daysTill", (due.getTime()-now.getTime())/(1000*60*60*24*1.0d));
				tasks.add(oo);
			}
		}
		Collections.sort(tasks, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				return Double.compare(o1.getDouble("daysTill"), o2.getDouble("daysTill"));
			}
		});
		
		sb.append(BASICMESSAGE+"\n\n");
		TableBuilder tb = new TableBuilder();
		tb.addNewlineAndTokens("название", "осталось дней");
		for(JSONObject obj:tasks) {
			tb.newRow();
			tb.addToken(com.github.nailbiter.util.Util.CutString(obj.getString("name"),parameters.getInt("margin")));
			double daysTill = obj.getDouble("daysTill");
			tb.addToken(util.Util.PrintDaysTill(daysTill,parameters.getString("filler")));
		}	
		sb.append(tb.toString());
		
		return sb.toString();
	}
}
