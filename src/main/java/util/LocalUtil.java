package util;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class LocalUtil {
	protected static boolean isInit = false;
	protected static boolean onMac = false;
	protected static String jarFolder; 
	protected static void init() throws Exception
	{
		if(!isInit)
		{
			onMac = System.getenv("HOME").startsWith("/Users");
			System.out.println("onMac="+onMac);
			isInit = true;
			if(onMac)
			{
				//FIXME
				String path = StorageManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
				String decodedPath = URLDecoder.decode(path, "UTF-8");
				jarFolder = decodedPath+"../../src/main/resources/";
			}
			else
			{
				String jarpath = System.getProperty("java.class.path");
				jarFolder = jarpath.substring(0, jarpath.lastIndexOf('/')+1);
			}
			System.out.println("jarFolder="+jarFolder);
		}
	}
	public static String getJarFolder() throws Exception { init(); return jarFolder;}
	public static JSONArray getJSONArrayFromRes(Object me,String name) throws Exception
	{
		init();
		System.out.println("name="+name);
		InputStream stream = me.getClass()
				.getClassLoader()
				.getResourceAsStream((onMac ? "" : "resources/")+name+".json");
        System.out.println(stream != null);
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer, "UTF8");
        String theString = writer.toString();
        System.out.println("theString="+theString);
        return (JSONArray)(new JSONTokener(theString)).nextValue();
	}
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
}
