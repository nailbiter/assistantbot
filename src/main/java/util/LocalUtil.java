package util;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLDecoder;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONTokener;

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
	public static String getJarFolder() {return jarFolder;}
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
