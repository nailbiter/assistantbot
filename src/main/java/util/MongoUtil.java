package util;

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

public class MongoUtil {
	public static JSONObject GetJsonObjectFromDatabase(MongoClient mc,String databasecollection) throws Exception {
		String[] split = databasecollection.split("\\.");
		if(split.length != 2)
			throw new Exception(String.format("could not split \"%s\" got len=%d", databasecollection,split.length));
		String database = split[0], collection = split[1];
		JSONObject res = new JSONObject(mc.getDatabase(database).getCollection(collection).find().first().toJson()); 
		System.err.format("%s with databasecollection=\"%s\" got %s\n", "GetJsonObjectFromDatabase",databasecollection,res.toString(2));
		return res;
	}
	public static JSONObject GetJsonObjectFromDatabase(MongoClient mc,String databasecollection,String keyValue) throws Exception {
		String database, collection, keyName, valueName;
		{
			String[] split = databasecollection.split("\\.");
			if(split.length != 2)
				throw new Exception(String.format("could not split \"%s\" got len=%d", databasecollection,split.length));
			database = split[0]; collection = split[1];
		}
		{
			String[] split = keyValue.split(":");
			if(split.length != 2)
				throw new Exception(String.format("could not split \"%s\" got len=%d", keyValue,split.length));
			keyName = split[0]; valueName = split[1];
		}
		System.err.format("db=%s, coll=%s,\nkey=%s, val=%s\n", database,collection,keyName,valueName);
		return new JSONObject(mc.getDatabase(database).getCollection(collection)
				.find(new Document(keyName,valueName))
				.first().toJson());
	}

	public static JSONArray GetJSONArrayFromDatabase(MongoClient mc, String databaseName, String collectionName, final String key) {
		final JSONArray res = new JSONArray();
		Block<Document> printBlock = new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   JSONObject obj = new JSONObject(doc.toJson());
		    	   res.put(obj.getString(key));
		       }
		};
		mc.getDatabase(databaseName).getCollection(collectionName).find().forEach(printBlock);
		
		return res;
	}

	public static JSONArray GetJSONArrayFromDatabase(MongoClient mc, String databaseName, String collectionName) {
		final JSONArray res = new JSONArray();
		Block<Document> printBlock = new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   JSONObject obj = new JSONObject(doc.toJson());
		    	   res.put(obj);
		       }
		};
		mc.getDatabase(databaseName).getCollection(collectionName).find().forEach(printBlock);
		
		return res;
	}

	public static JSONObject GetJsonObjectFromDatabase(MongoClient mc, String dc, JSONObject fo) throws Exception {
		String[] split = dc.split("\\.");
		if(split.length != 2)
			throw new Exception(String.format("could not split \"%s\" got len=%d", dc,split.length));
		String database = split[0], collection = split[1];
		System.err.format("GetJsonObjectFromDatabase: db=(%s), coll=(%s)", database,collection);
		return new JSONObject(mc.getDatabase(database).getCollection(collection).find(Document.parse(fo.toString())).first().toJson());
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
	public static final String LOGISTICS = "logistics";
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
}
