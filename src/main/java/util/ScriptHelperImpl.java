package util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;

import assistantbot.ResourceProvider;
import de.vandermeer.asciitable.AsciiTable;

public class ScriptHelperImpl implements ScriptHelper {
	private static final String METHODFIELDNAME = "method";
	private static final String DATAFIELDNAME = "data";
	private static final String ENDDATEKEY = "$enddate";
	private static final String STARTDATEKEY = "$startdate";
	private static final String TOTALVALUEKEY = "$total";
	private static String SIMPLEDATEFORMAT = "MM-dd-YYYY";
	private static enum TableEngine{
		ASCIITABLE, TABLEBUILDER,HTML
	};
	private Invocable inv_ = null;
	private MongoClient mongoClient_;
	private ResourceProvider rp_;
	private static JSONObject settings_;
	public ScriptHelperImpl(ResourceProvider rp) {
		mongoClient_ = rp.getMongoClient();
		rp_ = rp;
	}
	@Override
	public void setInvocable(Invocable inv) {
		inv_ = inv;
	}
	@Override
	public String execute(String arg) throws NoSuchMethodException, JSONException, ScriptException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException{
		JSONObject incoming = new JSONObject(arg);
		System.err.format("trying to dispatch for method \"%s\", object:%s\n", incoming.getString(METHODFIELDNAME),
				incoming.getJSONObject(DATAFIELDNAME).toString(2));
		return (String)this.getClass().getMethod(incoming.getString(METHODFIELDNAME), JSONObject.class)
				.invoke(this, incoming.getJSONObject(DATAFIELDNAME));
	}
	public void sendAsFile(JSONObject obj) throws JSONException, IOException, Exception {
		rp_.sendFile(Util.saveToTmpFile(obj.getString("content")));
	}
	public void addToDatabase(JSONObject jsonObject) {
		String[] split = jsonObject.getString("dbname").trim().split("\\.");
		System.out.format("database name: %s\n", split[0]);
		System.out.format("collection name: %s\n", split[1]);
		MongoCollection<Document> col = mongoClient_.getDatabase(split[0]).getCollection(split[1]);
		JSONArray array = jsonObject.getJSONArray("data");
		for(Object o:array) {
			JSONObject obj = (JSONObject)o;
			Document doc = new Document(obj.toMap());
			col.insertOne(doc);
		}
	}
	public void dropCollection(JSONObject jsonObject) {
		String[] split = jsonObject.getString("dbname").trim().split("\\.");
		System.err.format("database name: %s\n", split[0]);
		System.err.format("collection name: %s\n", split[1]);
		mongoClient_.getDatabase(split[0]).getCollection(split[1]).drop();		
	}
	public String printStatTableHTML(JSONObject jsonObject) throws Exception {
		System.err.format("%s was called with %s\n", "printStatTableHTML",jsonObject.toString(2));
		return PrintTable(jsonObject.getJSONArray("data"),jsonObject.getString("colname"),inv_,"recordValueToString",
				TableEngine.HTML);
	}
	public String printStatTablePLAIN(JSONObject jsonObject) throws Exception {
		System.err.format("%s was called with %s\n", "printStatTablePLAIN",jsonObject.toString(2));
		return PrintTable(jsonObject.getJSONArray("data"),jsonObject.getString("colname"),inv_,"recordValueToString",
				TableEngine.TABLEBUILDER);
	}
	public String getDataFromDatabase(JSONObject jsonObject) {
		System.err.format("%s was called with %s\n", "getDataFromDatabase",jsonObject.toString(2));
		String[] split = jsonObject.getString("dbname").trim().split("\\.");
		System.err.format("database name: %s\n", split[0]);
		System.err.format("collection name: %s\n", split[1]);
		
		MongoCollection<Document> col = mongoClient_.getDatabase(split[0]).getCollection(split[1]);
		
		FindIterable<Document> queryRes = col.find();
		if(jsonObject.has("sort")) {
			Bson sortOrder = getSortOrder(jsonObject.getJSONObject("sort"));
			queryRes = queryRes.sort(sortOrder);
		}
		
		final JSONArray res = new JSONArray();
		queryRes.forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				res.put(new JSONObject(arg0.toJson()));
			}
		});
		
		return res.toString();
	}
	private Bson getSortOrder(JSONObject jsonObject) {
		String key = jsonObject.keySet().iterator().next();
		if(jsonObject.getInt(key) > 0) {
			System.err.format("%s: return %s for key \"%s\"\n", "getSortOrder","ascending",key);
			return Sorts.ascending(key);
		} else {
			System.err.format("%s: return %s for key \"%s\"\n", "getSortOrder","descending",key);
			return Sorts.descending(key);
		}
	}
	public String printStatTableASCII(JSONObject jsonObject) throws Exception {
		System.err.format("%s was called with %s\n", "printStatTableASCII",jsonObject.toString(2));
		return PrintTable(jsonObject.getJSONArray("data"),jsonObject.getString("colname"),inv_,"recordValueToString",
				TableEngine.ASCIITABLE);
	}
	private static String PrintTable(JSONArray res, String databaseName, Invocable inv, String functionName, TableEngine te) throws Exception {
		final JSONObject total = new JSONObject();
		Object at = (te==TableEngine.ASCIITABLE) ? new AsciiTable():
			(te==TableEngine.TABLEBUILDER) ? new TableBuilder():
			(te==TableEngine.HTML) ? new StringBuilder(String.format("<table%s>",settings_.has("border")?
				String.format(" border=\"%s\"", settings_.getString("border")):"")) : null;
		for(Object o:res) {
			JSONObject obj = (JSONObject)o;
			int totalValue = 0;
			for(String key:obj.keySet())
				if(!key.equals(ENDDATEKEY) && !key.equals(STARTDATEKEY)) {
					totalValue += obj.getInt(key);
					if(!total.has(key))
						total.put(key, 0);
					total.put(key, total.getInt(key)+obj.getInt(key));
				}
			obj.put(TOTALVALUEKEY, totalValue);
		}
		ArrayList<String> categories = new ArrayList<String>();
		int totalValue = 0;
		for(String key:total.keySet()) {
			if(!key.equals(ENDDATEKEY) && !key.equals(STARTDATEKEY) && !key.equals(TOTALVALUEKEY))
				categories.add(key);
			totalValue += total.getInt(key);
		}
		total.put(TOTALVALUEKEY, totalValue);
		total.put(STARTDATEKEY, ((JSONObject)res.get(res.length()-1)).getLong(STARTDATEKEY));
		total.put(ENDDATEKEY, ((JSONObject)res.get(0)).getLong(ENDDATEKEY));
			
		Collections.sort(categories, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.compare(total.getInt(o1), total.getInt(o2));
			}
		});
		ArrayList<String> firstRow = new ArrayList<>();
		for(int i = 0; i < categories.size(); i++) {
			firstRow.add(categories.get(i));
//			firstRow.add("%");
		}
		firstRow.add("TOTAL");
//		firstRow.add("%");
		firstRow.add(0, "dates");
		if(te==TableEngine.ASCIITABLE) 
			((AsciiTable) at).addRule();
		AddRow(at,te,firstRow,true);
		if(te==TableEngine.ASCIITABLE) 
			((AsciiTable) at).addRule();
		for(int i = res.length()-1; i>=0; i--)
			PrintRecord(at,res.getJSONObject(i),categories,databaseName,inv,functionName,total,te,false);
		if(te==TableEngine.ASCIITABLE) 
			((AsciiTable) at).addRule();
		PrintRecord(at,total,categories,databaseName,inv,functionName,total,te,true);
		if(te==TableEngine.ASCIITABLE) 
			((AsciiTable) at).addRule();
		
		if(te==TableEngine.ASCIITABLE)
//			System.out.println(((AsciiTable) at).render());
			return ((AsciiTable) at).render();
		else if(te==TableEngine.TABLEBUILDER)
//			System.out.println((TableBuilder)at);
			return ((TableBuilder)at).toString();
		else if(te==TableEngine.HTML) {
			((StringBuilder)at).append("</table>");
//			System.out.println((StringBuilder)at);
			return ((StringBuilder)at).toString();
		} else 
			throw new Exception("Unknown type of TableEngine");
	}
	static void AddRow(Object table,TableEngine te, ArrayList<String> row, boolean isHeader){
		if(te==TableEngine.ASCIITABLE)
			((AsciiTable) table).addRow(row);
		else if(te==TableEngine.TABLEBUILDER) {
			((TableBuilder)table).newRow();
			for(int i = 0; i < row.size(); i++)
				((TableBuilder)table).addToken(row.get(i));
		} else if(te==TableEngine.HTML) {
			((StringBuilder)table).append("<tr>\n");
			for(int i = 0; i < row.size(); i++) {
				if( ( i == 0 ) || ( i == ( row.size() - 1 ) ) || isHeader )
					((StringBuilder)table).append(String.format("\t<th>%s</th>\n", row.get(i)));
				else
					((StringBuilder)table).append(String.format("\t<td>%s</td>\n", row.get(i)));
			}
			((StringBuilder)table).append("</tr>\n");
		}
	}
	private static void PrintRecord(Object at, JSONObject obj, ArrayList<String> categories, String databaseName, Invocable inv, String functionName, JSONObject total,TableEngine te, boolean isHeader) throws NoSuchMethodException, JSONException, ScriptException {
		ArrayList<String> row = new ArrayList<String>();
		SimpleDateFormat df = new SimpleDateFormat(SIMPLEDATEFORMAT );
		df.setTimeZone (TimeZone.getDefault());
		String procentFormat = null;
		if(te==TableEngine.HTML)
			procentFormat = "<b>%4.2f</b>";
		else
			procentFormat = "%4.2f";
		row.add(String.format("%s..%s", 
				df.format(new Date(obj.getLong(STARTDATEKEY))),
				df.format(new Date(obj.getLong(ENDDATEKEY)))));
		for(String category:categories) {
			System.err.format("fn=%s, obj=%s, dn=%s\n", functionName,obj.toString(),databaseName);
			row.add(Util.PrintTooltip((String)inv.invokeFunction(functionName,obj.optInt(category),databaseName), 
					String.format(procentFormat, (100.0*obj.optInt(category))/obj.getInt(TOTALVALUEKEY))));
		}
		row.add(Util.PrintTooltip((String)inv.invokeFunction(functionName,obj.getInt(TOTALVALUEKEY),databaseName), 
				String.format(procentFormat, (100.0*obj.getInt(TOTALVALUEKEY))/total.getInt(TOTALVALUEKEY))));
		AddRow(at,te,row,isHeader);
	}
	public void setParamObject(JSONObject settings) {
		settings_ = settings;
	}
}
