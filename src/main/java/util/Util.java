package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import managers.MyManager;

import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.ResourceProvider;

/**
 * 
 * @author oleksiileontiev
 * No dependency on telegram here
 */
public class Util{
	protected static boolean isInit = false;
	protected static String jarFolder;
	private static String RebootFileName_;
	private static String rebootCommandFileName_; 
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
		df.setTimeZone(Util.getTimezone());
		return df.format(d);
	}
	public static void SetRebootFileName(String string) {
		RebootFileName_ = string;
	}
	public static String GetRebootFileName() {
		return RebootFileName_;
	}
	public static String GetFile(String name) throws Exception{
		FileReader fr = null;
		String fname = name;
		StorageManager.logger_.info(String.format("fname=%s", fname));
		
		fr = new FileReader(fname);
		StringBuilder sb = new StringBuilder();
	    int character;
	    while ((character = fr.read()) != -1) {
	    		sb.append((char)character);
	    }
	    System.err.println("found "+sb.toString());
		fr.close();
		String res = sb.toString();
		StorageManager.logger_.info(String.format("res=%s", res));
		return res;
	}
	public static String GetFileFromAssistantBotFiles(String name) throws Exception
	{
		FileReader fr = null;
		String fname = getJarFolder()+name;
		StorageManager.logger_.info(String.format("fname=%s", fname));
		
		fr = new FileReader(fname);
		StringBuilder sb = new StringBuilder();
	    int character;
	    while ((character = fr.read()) != -1) {
	    		sb.append((char)character);
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
	public static void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
	public static String runScript(String cmd) throws Exception{
		System.out.println("going to run: "+cmd);
		StringBuilder res = new StringBuilder();
		try {
		Runtime run = Runtime.getRuntime();
	    Process pr = run.exec(cmd);
	    pr.waitFor();
	    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
	    String line = "";
	    while ((line=buf.readLine())!=null) {
	            res.append(line+"\n");
	    }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("here with: "+res.toString());
	    return res.toString();
	}
	public static void SetRebootCommandFileName(String string) {
		rebootCommandFileName_ = string;
	}
	public static String GetRebootCommandFileName() {
		return rebootCommandFileName_ ;
	}
	public static void PopulateManagers(List<MyManager> managers,JSONArray names,ResourceProvider rp) throws Exception {
		for(Object o:names) {
			String name = (String)o;
			if(name==null)
				continue;
			String cn = String.format("%s.%s","managers", name);
			try {
				Class<?> clazz = Class.forName(cn);
				Constructor<?> constructor = clazz.getConstructor(ResourceProvider.class);
				Object instance = constructor.newInstance(rp);
				managers.add((MyManager) instance);
			}
			catch(Exception e) {
				e.printStackTrace();
				System.err.format("cannot instantiate %s\n", cn);
				throw e;
			}
			System.err.format("added %s\n", cn);
		}
	}
}
