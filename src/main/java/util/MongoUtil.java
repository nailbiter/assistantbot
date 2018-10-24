package util;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;

public class MongoUtil {
	public static JSONObject GetJsonObjectFromDatabase(MongoClient mc,String databasecollection) throws Exception {
		String[] split = databasecollection.split("\\.");
		if(split.length != 2)
			throw new Exception(String.format("could not split \"%s\" got len=%d", databasecollection,split.length));
		String database = split[0], collection = split[1];
		return new JSONObject(mc.getDatabase(database).getCollection(collection).find().first().toJson());
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
}
