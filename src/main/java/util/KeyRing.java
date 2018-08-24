package util;

import org.json.JSONObject;

public class KeyRing {
	protected static boolean isInit = false;
	static String passwd;
	static JSONObject obj;
	protected static void init()
	{
		if(!isInit)
		{
			obj = StorageManager.get("keyring", false);
//			token = obj.getString("bottoken");
			passwd = obj.getString("passwd");
			isInit = true;
		}
	}
	public static String getString(String key) {
		init();
		return obj.getString(key);
	}
	
	public static String getPasswd()
	{
		init();
		return passwd;
	}
}
