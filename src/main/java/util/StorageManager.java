package util;

import java.io.FileReader;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class StorageManager {
	protected static Hashtable<String,JSONObject> registeredObjects = new Hashtable<String,JSONObject>();
	protected static MyManager myManager = null;
	public static MyManager getMyManager() {return myManager;}
	public static void init() throws Exception
	{
		myManager = new MyManager()
		{

			@Override
			public String getResultAndFormat(JSONObject res) throws Exception {
				if(res.has("name"))
				{
					System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
					if(res.getString("name").compareTo("dump")==0)
					{
						onShutdown();
						StringBuilder sb = new StringBuilder();
						sb.append("dumped:\n");
						Iterator<String> itr = registeredObjects.keySet().iterator();
				    		String str;
				    		while (itr.hasNext()) {
			    			 try {
			    				   str = itr.next();
			    				   sb.append("\t"+str+"\n");
			    			 }
			    			 catch(Exception e)
			    			 {
			    				 e.printStackTrace(System.out);
			    			 }
			    		    }
				    		return sb.toString();
					}
				}
				return null;
			}

			@Override
			public String gotUpdate(String data) throws Exception {
				return null;
			}

			@Override
			public JSONArray getCommands() {
				return new JSONArray("[{\"name\":\"dump\",\"args\":[],\"help\":\"dump all text files\"}]");
			}

			@Override
			public String processReply(int messageID,String msg) {
				return null;
			}
	
		};
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
