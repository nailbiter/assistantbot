package util;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoUtil {
	public static JSONObject GetJsonObjectFromDatabase(MongoClient mc,String databasecollection) throws Exception {
		String[] split = databasecollection.split("\\.");
		if(split.length != 2)
			throw new Exception(String.format("could not split \"%s\" got len=%d", databasecollection,split.length));
		String database = split[0], collection = split[1];
		return new JSONObject(mc.getDatabase(database).getCollection(collection).find().first().toJson());
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
			System.out.format("EXCEPTION!\n");
		}
		return new MongoClient(uri);
	}
}
