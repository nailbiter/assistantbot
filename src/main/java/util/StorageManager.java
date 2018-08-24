package util;

import java.io.FileReader;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONTokener;

public class StorageManager {
	protected static Hashtable<String,JSONObject> registeredObjects = new Hashtable<String,JSONObject>();
	public static void init() throws Exception
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
	    {
	        @Override
	        public void run()
	        {
	            System.out.println("Shutdown hook ran!");
	            onShutdown();
	        }});
	}
	public static JSONObject get(String name,boolean register)
	{
		FileReader fr = null;
		JSONObject res = null;
		System.out.println(String.format("%s.get(%s,%s)", StorageManager.class.getName(),name,register));
		try {
			System.out.println("StorageManager got "+name);
			String fname = LocalUtil.getJarFolder()+name+".json";
			System.out.println("storageManager gonna open: "+fname);
			fr = new FileReader(fname);
			StringBuilder sb = new StringBuilder();
            int character;
            while ((character = fr.read()) != -1) {
            		sb.append((char)character);
                //System.out.print((char) character);
            }
            System.out.println("found "+sb.toString());
			fr.close();
			res = (JSONObject) (new JSONTokener(sb.toString())).nextValue();
		}
		catch(Exception e) {
			System.out.println("found nothing");
			res = new JSONObject();
		}
		if(register)
			register(name,res);
		return res;
	}
	protected static void register(String name, JSONObject ref)
	{
		System.out.println("register "+name);
		registeredObjects.put(name, ref);
	}
	protected static void onShutdown()
	{
		Iterator<String> itr = registeredObjects.keySet().iterator();
		String str;
		FileWriter fw;
		System.out.println("was here on shutdown");
		while (itr.hasNext()) {
			 try {
				   str = itr.next();
			       System.out.println("Key: "+str+" & Value: "+registeredObjects.get(str));
			       fw = new FileWriter(LocalUtil.getJarFolder()+str+".json");
			       fw.write(registeredObjects.get(str).toString());
			       fw.close(); 
			 }
			 catch(Exception e)
			 {
				 e.printStackTrace(System.out);
			 }
		    }
	}
}
