package util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

public class LocalUtil {
	protected static boolean isInit = false;
	protected static String jarFolder;
	private static String RebootFileName_; 
	public static void SetJarFolder(String jf) {jarFolder = jf;}
	protected static void init() throws Exception
	{
	}
	public static String getJarFolder() throws Exception { init(); return jarFolder;}
	public static String milisToTimeFormat(long millis)
	{
		return Integer.toString((int)(millis/1000.0/60.0/60.0)) + "h:"+
				Integer.toString((int)((millis/1000.0/60.0)%60)) + "m:"+
				Integer.toString((int)((millis/1000.0)%60)) + "s";
	}
	public static TimeZone getTimezone() throws Exception
	{
		TimeZone tz = TimeZone.getTimeZone("JST");
		System.out.println("zone: "+tz.getID());
		return tz;
	}
	public static String DateToString(Date d) throws Exception
	{
		DateFormat df = new SimpleDateFormat();
		df.setTimeZone(LocalUtil.getTimezone());
		return df.format(d);
	}
	public static void SortJSONArray(JSONArray arr,String key)
	{
		List<JSONObject> list = new ArrayList<JSONObject>();
		final String keyFinal = key;
		for(int idx = 0; idx < arr.length(); )
		{
			JSONObject obj = arr.optJSONObject(idx);
			if(obj==null)
				idx++;
			else
			{
				arr.remove(idx);
				list.add(obj);
			}
		}
		Collections.sort(list,new Comparator<JSONObject>()
				{
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						return o1.getString(keyFinal).compareTo(o2.getString(keyFinal));
					}
				});
		for(int i = 0; i < list.size(); i++)
			arr.put(list.get(i));
	}
	public static void SetRebootFileName(String string) {
		RebootFileName_ = string;
	}
	public static String GetRebootFileName() {
		return RebootFileName_;
	}
}
