package util;

import org.json.JSONObject;

public class KeyRing {
	protected static boolean isInit = false;
	static String token,passwd;
	static JSONObject obj_ = null;
	protected static void init()
	{
		if(!isInit)
		{
			obj_ = StorageManager.get("keyring",false);
			token = obj_.getString("bottoken");
			passwd = obj_.getString("passwd");
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
	public static String getMailPassword()
	{
		init();
		return obj_.getString("mailpassword");
	}
	public static String getMail(int i)
	{
		init();
		return obj_.getJSONArray("mails").getString(i);
	}
}
