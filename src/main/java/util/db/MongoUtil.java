package util.db;

import static java.lang.Integer.parseInt;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import assistantbot.ResourceProvider;
import util.SettingCollection;
import util.Util;

public class MongoUtil {
	private static final String LOGISTICS = "logistics";
	
	public static JSONArray GetJSONArrayFromDatabase(MongoCollection<Document> coll) {
		final JSONArray res = new JSONArray();
		coll.find().forEach(new Block<Document>() {
			@Override
			public void apply(Document doc) {
				res.put(new JSONObject(doc.toJson()));
			}
		});
		return res;
	}
	public static MongoClient GetMongoClient(String password) {
		String url = String.format("mongodb://%s:%s@ds149672.mlab.com:49672/logistics", 
	            "nailbiter",password);
		MongoClientURI uri = null;
		try {
			uri = new MongoClientURI(url);
		}
		catch(Exception e) {
			System.err.format("EXCEPTION!\n");
		}
		return new MongoClient(uri);
	}
	public static Date MongoDateStringToLocalDate(String string) throws Exception {
		Matcher m = null;
		if((m = Pattern.compile("[A-Z][a-z]{2} ([A-Z][a-z]{2}) (\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) ([A-Z]{3}) (\\d{4})").matcher(string)).matches()) {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone(m.group(6)));
			c.set(parseInt(m.group(7)),
					Util.MONTHNAMES.getInt(m.group(1))-1,
					parseInt(m.group(2)),
					parseInt(m.group(3)),
					parseInt(m.group(4)),
					parseInt(m.group(5))
					);
			return c.getTime();
		} else {
			throw new Exception(String.format("cannot parse %s", string));
		}
	}
	@SuppressWarnings("deprecation")
	public static MongoCollection<Document> GetSettingCollection(ResourceProvider rp,SettingCollection sc){
		return GetSettingCollection(rp.getMongoClient(),sc);
	}
	public static MongoCollection<Document> GetSettingCollection(MongoClient cli,SettingCollection sc){
		return cli.getDatabase(LOGISTICS).getCollection(sc.toString());
	}
	public static JSONObject GetJsonObjectFromDatabase(MongoCollection<Document> coll, String key,Object val) {
		if( key == null || val == null ) {
			return new JSONObject(coll.find().first().toJson());
		} else {
			System.err.format("key=%s, val=%s\n", key,val.toString());
			return new JSONObject(coll
					.find(Filters.eq(key,val))
					.first().toJson());
		}
		
	}
	/**
	 * @deprecated
	 * @return
	 */
	public static String getLogistics() {
		return LOGISTICS;
	}
}
