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
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import managers.MyManager;
import util.AssistantBotException.Type;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.github.nailbiter.util.TableBuilder;

import assistantbot.ResourceProvider;

/**
 * 
 * @author oleksiileontiev
 * No dependency on telegram here
 */
public class Util{
	protected static boolean isInit = false;
	private final static String ALPH = "abcdefghijklmnopqrstuvwxyz" +
			"abcdefghijklmnopqrstuvwxyz".toUpperCase() + "01234567890";
	public final static JSONObject MONTHNAMES = new JSONObject("{\"Jan\":1,"
			+ "\"Feb\":2,\"Mar\":3,\"Apr\":4,\"May\":5,"
			+ "\"Jun\":6,\"Jul\":7,\"Aug\":8,\"Sep\":9,\"Oct\":10,\"Nov\":11,"
			+ "\"Dec\":12}");
	private static final int MAXLINENUMINMESSAGE = 41;
	public static final String NAMEFIELDNAME = "name";
	private static Random rand_ = new Random();
	private static JSONObject profileObj_;
	protected static void init() throws Exception {
	}
	public static enum EnvironmentParameter {
		TMPFOLDER("TMPFOLDER")
		,TMPFILE("TMPFILE")
		,PARSERPREFIX("PARSERPREFIX")
		,SCRIPTFOLDER("SCRIPTFOLDER")
		,CLIENT("CLIENT")
		;
		private final String name_;
		private EnvironmentParameter(String name) {
			name_ = name;
		}
		public String toString() {
			return name_;
		}
	};
	public static void setProfileObj(String string) throws Exception {
		profileObj_ = new JSONObject(string);
		JSONArray keys = new JSONArray();
		for(EnvironmentParameter ep:EnvironmentParameter.values())
			keys.put(ep.toString());
		JsonUtil.FilterJsonKeys(profileObj_,keys);
		for(Object key:keys) {
			if(null==profileObj_.getString((String)key))
				throw new Exception();
		}
	}
	public static String Gss(EnvironmentParameter ep) {
		return Gss(ep.toString());
	}
	/**
	 * @deprecated
	 * @param name
	 * @return
	 */
	protected static String Gss(String name) {
		return profileObj_.getString(name.toUpperCase());
	}
	/**
	 * @deprecated
	 * @return
	 */
	public static String GetClientType() {
		return Gss("CLIENT");
	}
	/**
	 * @deprecated
	 * @return
	 */
	public static String getScriptFolder() {
		return Gss("SCRIPTFOLDER");
	}
	/**
	 * @deprecated
	 * @return
	 */
	public static String getParsePrefix() {
		return Gss("PARSERPREFIX");
	}
	/**
	 * @deprecated
	 * @return
	 */
	public static String getJarFolder(){ 
		return Gss("RESFOLDER");
	}
	public static String milisToTimeFormat(long millis)
	{
		return Integer.toString((int)(millis/1000.0/60.0/60.0)) + "h:"+
				Integer.toString((int)((millis/1000.0/60.0)%60)) + "m:"+
				Integer.toString((int)((millis/1000.0)%60)) + "s";
	}
	/**
	 * @deprecated
	 * @return
	 */
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
//		FileReader fr = null;
//		String fname = getJarFolder()+name;
//		StorageManager.logger_.info(String.format("fname=%s", fname));
//		
//		fr = new FileReader(fname);
//		StringBuilder sb = new StringBuilder();
//	    int character;
//	    while ((character = fr.read()) != -1) {
//	    		sb.append((char)character);
//	    }
//	    System.out.println("found "+sb.toString());
//		fr.close();
//		String res = sb.toString();
//		StorageManager.logger_.info(String.format("res=%s", res));
//		return res;
		return null;
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
				Class<?> managerClass = Class.forName(cn);
				Constructor<?> managerConstructor = 
						managerClass.getConstructor(ResourceProvider.class);
				Object managerInstance = managerConstructor.newInstance(rp);
				managers.add((MyManager) managerInstance);
			} catch(Exception e) {
				e.printStackTrace();
				System.err.format("cannot instantiate %s!\n", cn);
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
	public static String GetTmpFilePath(String ext) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < 10;i++)
			sb.append(ALPH.charAt(rand_.nextInt(ALPH.length())));
		return String.format("%s/%s%s", Gss("TMPFOLDER"), sb.toString(), ext);
	}
	public static String saveToTmpFile(String content) throws IOException {
		String filePath = GetTmpFilePath(".html");
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
	/**
	 * @deprecated use RunScript instead
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public static String ExecuteCommand(String command) throws IOException{
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(command);
	
		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));
	
//		BufferedReader stdError = new BufferedReader(new 
//		     InputStreamReader(proc.getErrorStream()));
	
		// read the output from the command
		System.err.println(String.format("Here is the standard output of the command \"%s\":\n",command));
		String s = null;
		StringBuilder sb = new StringBuilder();
		while ((s = stdInput.readLine()) != null) {
			sb.append(s+"\n");
		}
		return sb.toString().trim();
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
	public static double DaysTill(String string) throws ParseException {
		SimpleDateFormat DF = com.github.nailbiter.util.Util.GetTrelloDateFormat();
		Date due = DF.parse(string),
				now = new Date();
		return (due.getTime()-now.getTime())/(1000*60*60*24*1.0d);
	}
	public static TableBuilder JsonObjectToTable(JSONObject obj) {
		TableBuilder tb = new TableBuilder();
		for(String key:obj.keySet()) {
			tb.newRow();
			tb.addToken(key);
			tb.addToken(obj.get(key).toString());
		}
		return tb;
	}
	public static Set<String> StringToSet(String string) {
		Set<String> res = new HashSet<String>();
		for(int i = 0; i < string.length(); i++)
			res.add(string.substring(i, i+1));
		return res;
	}
	public static String CheckMessageLen(String msg) {
		String[] split = msg.split("\n");
		System.err.format("split.size=%d\n", split.length);
		
		if(split.length <= MAXLINENUMINMESSAGE) {
			return msg;
		} else {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < (MAXLINENUMINMESSAGE-1); i++) {
				sb.append(split[i]+"\n");
			}
			sb.append("...");
			return sb.toString();
		}
		
	}
	public static <T> ArrayList<T> GetArrayHead(ArrayList<T> arr, int maxsize) {
		ArrayList<T> res = new ArrayList<T>();
		for(int i = 0; i < maxsize && i < arr.size(); i++)
			res.add(arr.get(i));
		return res;
	}
	public static <A,B> List<B> Map(List<A> l,Transformer<A,B> t){
		ArrayList<B> res = new ArrayList<B>();
		for(A a:l)
			res.add(t.transform(a));
		return res;
	}
	public static JSONObject GetDefaultUser() throws JSONException, Exception {
		JSONObject res = new JSONObject(GetFile(Util.USERSFILE))
				.getJSONObject(Util.DEFAULTUSERNAME);
		res.put(NAMEFIELDNAME, Util.DEFAULTUSERNAME);
		return res;
	}
	private static final String DEFAULTUSERNAME = "alex";
	/**
	 * @deprecated move to environment
	 */
	private static final String USERSFILE = "src/main/resources/params/userRecords.json";
	public static Map<String, Object> IdentityMap(JSONArray names) {
		Map<String, Object> res = new Hashtable<String, Object>();
		for(Object o:names)
			res.put((String)o, (String)o);
		return res;
	}
	public static Map<String, Object> AppendObjectMap(JSONArray names,Object obj){
		Map<String, Object> res = new Hashtable<String, Object>();
		for(Object o:names) {
			ImmutablePair<String,Object> pair = new ImmutablePair<String,Object>((String)o,obj); 
			res.put((String)o, pair);
		}
			
		return res;
	}
	public static String CharSetToRegex(Collection<String> s) {
		StringBuilder sb = new StringBuilder();
		for(String str:s) {
			String ss = str;
			if(str.equals("."))
				ss = "\\.";
			sb.append(ss);
		}
		return sb.toString();
	}
	public static PhotoSize GetPhoto(Message msg) {
	    // Check that the update contains a message and the message has a photo
	    if (msg.hasPhoto()) {
	        // When receiving a photo, you usually get different sizes of it
	        List<PhotoSize> photos = msg.getPhoto();
	
	        // We fetch the bigger photo
	        return photos.stream()
	                .sorted(
	                		new Comparator<PhotoSize>() {
								@Override
								public int compare(PhotoSize o1, PhotoSize o2) {
									return Integer.compare(o1.getFileSize(), o2.getFileSize());
								}
	                		}
	        		.reversed())
	                .findFirst()
	                .orElse(null);
	    }
	    
	
	    // Return null if not found
	    return null;
	}
	public static String ExceptionToString(Exception e) {
		return String.format("e: %s", e.getMessage());
	}
	public static Map<String,Object> IdentityMapWithSuffix(JSONArray names, int indexOf, String suffix) {
		Map<String, Object> res = new Hashtable<String, Object>();
		for(int i = 0; i < names.length(); i++) {
			String o = names.getString(i);
			if( i == indexOf )
				res.put((String)o+suffix, (String)o);
			else
				res.put((String)o, (String)o);
		}
			
		return res;
	}
	public static String DateToString(Date d, TimeZone tz) throws Exception
	{
		DateFormat df = new SimpleDateFormat();
		df.setTimeZone(tz);
		return df.format(d);
	}
}
