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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import managers.MyManager;
import util.AssistantBotException.Type;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import static java.lang.Integer.parseInt;

/**
 * 
 * @author oleksiileontiev
 * No dependency on telegram here
 */
public class Util{
	protected static boolean isInit = false;
	private final static String ALPH = "abcdefghijklmnopqrstuvwxyz" +
			"abcdefghijklmnopqrstuvwxyz".toUpperCase() + "01234567890";
	private final static JSONObject MONTHNAMES = new JSONObject("{\"Jan\":1,"
			+ "\"Feb\":2,\"Mar\":3,\"Apr\":4,\"May\":5,"
			+ "\"Jun\":6,\"Jul\":7,\"Aug\":8,\"Sep\":9,\"Oct\":10,\"Nov\":11,"
			+ "\"Dec\":12}");
	private static Random rand_ = new Random();
	private static JSONObject profileObj_;
	protected static void init() throws Exception
	{
	}
	public static void setProfileObj(String string) throws Exception {
		profileObj_ = new JSONObject(string);
		JSONArray keys = new JSONArray()
				.put("RESFOLDER")
				.put("TMPFOLDER")
				.put("CMDFILE")
				.put("TMPFILE")
				.put("PARSERPREFIX")
				.put("SCRIPTFOLDER");
		JsonUtil.FilterJsonKeys(profileObj_,keys);
		for(Object key:keys) {
			if(null==profileObj_.getString((String)key))
				throw new Exception();
		}
	}
	protected static String Gss(String name) {
		return profileObj_.getString(name.toUpperCase());
	}
	public static String getScriptFolder() {
		return Gss("SCRIPTFOLDER");
	}
	public static String getParsePrefix() {
		return Gss("PARSERPREFIX");
	}
	public static String getJarFolder(){ 
		return Gss("RESFOLDER");
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
		df.setTimeZone(Util.getTimezone());
		return df.format(d);
	}
	public static String GetRebootFileName() {
		return Gss("TMPFILE");
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
	public static String RunScript(String cmd) throws Exception{
		System.err.println("going to run: "+cmd);
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
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.err.println("here with: "+res.toString());
	    return res.toString();
	}
	public static String GetRebootCommandFileName() {
		return profileObj_.getString("CMDFILE");
	}
	public static String AddTerminalSlash(String dirname) {
		if(dirname.endsWith("/"))
			return dirname;
		else
			return dirname+"/";
	}
	public static void PopulateManagers(List<MyManager> managers,JSONArray names,ResourceProvider rp) throws Exception {
		for(Object o:names) {
			String name = null;
			if(o instanceof String) {
				name = (String)o;
			} else if(o instanceof JSONObject) {
				name = ((JSONObject)o).getString("name");
			}
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
			}
			System.err.format("added %s\n", cn);
		}
	}
	public static JSONObject GetDefSettingsObject(JSONArray names) {
		JSONObject res = new JSONObject();
		for(Object o:names) {
			if(o instanceof JSONObject) {
				JSONObject obj = (JSONObject)o;
				for(Iterator<String> it = obj.keys();it.hasNext();) {
					String key = it.next();
					if(key.startsWith("DEF"))
						res.put(key, obj.getString(key));
				}
			}
		}
		return res;
	}
	public static String saveToTmpFile(String content) throws IOException {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < 10;i++)
			sb.append(ALPH.charAt(rand_.nextInt(ALPH.length())));
		String filePath = String.format("%s/%s.%s", profileObj_.getString("TMPFOLDER"),sb.toString(),"html");
		BufferedWriter writer = null;
		try
		{
		    writer = new BufferedWriter(new FileWriter(filePath));
		    writer.write(content);
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
		return filePath;
	}
	private static void CopyFileUsingStream(File source, File dest) throws IOException {
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
	public static String ExecuteCommand(String command) throws IOException{
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(command);
	
		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));
	
		BufferedReader stdError = new BufferedReader(new 
		     InputStreamReader(proc.getErrorStream()));
	
		// read the output from the command
		System.out.println(String.format("Here is the standard output of the command \"%s\":\n",command));
		String s = null;
		StringBuilder sb = new StringBuilder();
		while ((s = stdInput.readLine()) != null) {
			sb.append(s+"\n");
		}
		return sb.toString().trim();
	}
	public static Date MongoDateStringToLocalDate(String string) throws Exception {
		Matcher m = null;
		if((m = Pattern.compile("[A-Z][a-z]{2} ([A-Z][a-z]{2}) (\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) ([A-Z]{3}) (\\d{4})").matcher(string)).matches()) {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone(m.group(6)));
			c.set(parseInt(m.group(7)),
					MONTHNAMES.getInt(m.group(1))-1,
					parseInt(m.group(2)),
					parseInt(m.group(3)),
					parseInt(m.group(4)),
					parseInt(m.group(5))
					);
			return c.getTime();
		} else {
			throw new Exception(String.format("cannot parse %s", string));
		}
	}
	public static String PrintDaysTill(double daysTill, String filler) {
		if(daysTill<0) {
			return (String.format("** %.3f **", daysTill));
		} else if(daysTill<1) {
			return (String.format("%.3f", daysTill));
		} else {
			return (StringUtils.repeat(filler,(int)daysTill));
		}
	}
	public static String PrintTooltip(String tip,String text) {
		return String.format("<div class=\"tooltip\">%s\n" + 
				"				  <span class=\"tooltiptext\">%s</span>\n" + 
				"				</div> ", text,tip);
	}
	public static int SimpleEval(String expr) throws AssistantBotException {
		if( !Pattern.matches("[0-9+*]+", expr)) {
			throw new AssistantBotException(AssistantBotException.Type.ARITHMETICPARSE,String.format("cannot eval \"%s\"", expr));
		}
		String[] split = expr.split("\\+");
		int res = 0;
		for(String part:split) {
			int res1 = 1;
			String[] split1 = part.split("\\*");
			for(String part1:split1) {
				res1 *= Integer.parseInt(part1); 
			}
			res += res1;
		}
			
		return res;
	}
	public static double DaysTill(String string) throws ParseException {
		SimpleDateFormat DF = com.github.nailbiter.util.Util.GetTrelloDateFormat();
		Date due = DF.parse(string),
				now = new Date();
		return (due.getTime()-now.getTime())/(1000*60*60*24*1.0d);
	}
}
