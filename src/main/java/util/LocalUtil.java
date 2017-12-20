package util;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONTokener;

public class LocalUtil {
	protected static boolean isInit = false;
	protected static boolean onMac = false;
	protected static void init()
	{
		if(!isInit)
		{
			onMac = System.getenv("HOME").startsWith("/Users");
			System.out.println("otMac="+onMac);
			isInit = true;
		}
	}
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
}
