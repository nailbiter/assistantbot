package util;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;

/**
 * 
 * @author oleksiileontiev
 * No dependency on telegram here
 */
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
	public static JSONArray GetJSONArrayFromDatabase(MongoClient mc, String databaseName, String collectionName) {
		final JSONArray res = new JSONArray();
		Block<Document> printBlock = new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   JSONObject obj = new JSONObject(doc.toJson());
		    	   res.put(obj);
		       }
		};
		mc.getDatabase(databaseName).getCollection(collectionName).find().forEach(printBlock);
		
		return res;
	}
	public static JSONArray GetJSONArrayFromDatabase(MongoClient mc, String databaseName, String collectionName, final String key) {
		final JSONArray res = new JSONArray();
		Block<Document> printBlock = new Block<Document>() {
		       @Override
		       public void apply(final Document doc) {
		    	   JSONObject obj = new JSONObject(doc.toJson());
		    	   res.put(obj.getString(key));
		       }
		};
		mc.getDatabase(databaseName).getCollection(collectionName).find().forEach(printBlock);
		
		return res;
	}
	public static String GetFile(String name) throws Exception
	{
		FileReader fr = null;
		String fname = getJarFolder()+name;
		StorageManager.logger_.info(String.format("fname=%s", fname));
		
		fr = new FileReader(fname);
		StringBuilder sb = new StringBuilder();
	    int character;
	    while ((character = fr.read()) != -1) {
	    		sb.append((char)character);
	        //System.out.print((char) character);
	    }
	    System.out.println("found "+sb.toString());
		fr.close();
		String res = sb.toString();
		StorageManager.logger_.info(String.format("res=%s", res));
		return res;
	}
	public static void SaveJSONObjectToFile(String filePath, JSONObject obj) throws IOException {
		BufferedWriter writer = null;
		try
		{
		    writer = new BufferedWriter(new FileWriter(filePath));
		    writer.write(obj.toString());
		}
		catch ( IOException e)
		{
			throw e;
		}
		finally
		{
		    try
		    {
		        if ( writer != null)
		        writer.close( );
		    }
		    catch ( IOException e)
		    {
		    	throw e;
		    }
		}
	}
}
