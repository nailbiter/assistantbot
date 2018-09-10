package util;

import org.json.JSONObject;

public class KeyRing {
//	protected static boolean isInit = false;
	static String token,passwd;
	static JSONObject obj_ = null;
	public static void init(String name)
	{
//		if(!isInit)
//		{
		obj_ = StorageManager.get("keyring",false);
		token = obj_.getJSONObject("telegramtokens").getString(name);
		passwd = obj_.getString("passwd");
//		isInit = true;
//		}
	}
	public static String getToken()
	{
//		init();
		return token;
	}
	public static String getPasswd()
	{
//		init();
		return passwd;
	}
	public static String getMailPassword()
	{
//		init();
		return obj_.getString("mailpassword");
	}
	public static String get(String key) {
//		init();
		return obj_.getString(key);
	}
}
