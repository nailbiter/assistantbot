package util;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import util.db.MongoUtil;

public class KeyRing {
	static String token,passwd;
	static JSONObject obj_ = null;
	public static void init(String name, MongoClient mongoClient) throws Exception
	{
		MongoCollection<Document> coll = 
				MongoUtil.GetSettingCollection(mongoClient, SettingCollection.KEYRING);
		obj_ = MongoUtil.GetJsonObjectFromDatabase(coll, null, null);
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
	public static String getString(String key) {
		return obj_.getString(key);
	}
	public static String getInt(String key) {
		return obj_.getString(key);
	}
	public static JSONObject getTrello() {
		return obj_.getJSONObject("trello");
	}
}
