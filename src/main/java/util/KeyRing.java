package util;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;

public class KeyRing {
	static String token,passwd;
	static JSONObject obj_ = null;
	public static void init(String name, MongoClient mongoClient)
	{
		Block<Document> printBlock = new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   obj_ = new JSONObject(doc.toJson());
		       }
		};
		mongoClient.getDatabase("logistics").getCollection("keyring").find().limit(1).forEach(printBlock);
		token = obj_.getJSONObject("telegramtokens").getString(name);
		passwd = obj_.getString("passwd");
	}
	public static String getToken()
	{
		return token;
	}
	public static String getPasswd()
	{
		return passwd;
	}
	public static String getMailPassword()
	{
		return obj_.getString("mailpassword");
	}
	public static String get(String key) {
		return obj_.getString(key);
	}
	public static JSONObject getTrello() {
		return obj_.getJSONObject("trello");
	}
}
