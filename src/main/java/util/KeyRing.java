package util;

import org.json.JSONObject;

public class KeyRing {
	protected static boolean isInit = false;
	static String token,passwd;
	protected static void init()
	{
		if(!isInit)
		{
			JSONObject obj = StorageManager.get("keyring",false);
			token = obj.getString("bottoken");
			passwd = obj.getString("passwd");
			isInit = true;
		}
	}
	public static String getToken()
	{
		init();
		return token; //assistantBot
	}
	public static String getPasswd()
	{
		init();
		return passwd;
	}
}
