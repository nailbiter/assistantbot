/**
 * 
 */
package util.parsers;

import org.json.JSONArray;
import org.json.JSONObject;

import util.StorageManager;
import util.Util;

/**
 * @author nailbiter
 *
 */
public class BotManagerParser implements AbstractParser {
	JSONArray help = null;
	public BotManagerParser()
	{
		JSONObject obj = StorageManager.get("help",false);
		help = obj.getJSONArray("help");
	}

	/* (non-Javadoc)
	 * @see util.MyManager#getResultAndFormat(org.json.JSONObject)
	 */
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("help")==0)
				return getHelpMessage();
			if(res.getString("name").compareTo("bash")==0)
				return Util.runScript(res.getString("line"));
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see util.MyManager#gotUpdate(java.lang.String)
	 */
	@Override
	public String gotUpdate(String data) throws Exception {
		return null;
	}

	/* (non-Javadoc)
	 * @see util.parsers.AbstractParser#parse(java.lang.String)
	 */
	@Override
	public JSONObject parse(String line) throws Exception {
		JSONObject res = new JSONObject();
		String[] tokens = line.split(" ");
		
		if(tokens.length==0)
		{
			res.put("cmd", line);
		}
		else if(tokens[0].equals("/login"))
		{
			res.put("name", "login");
			res.put("passwd", tokens[1]);
		}
		else if(tokens[0].equals("/help"))
		{
			res.put("name", "help");
		}
		else if(tokens[0].startsWith("/"))
		{
			StringBuilder sb = new StringBuilder();
			for(int i = 1; i < tokens.length; i++)
				sb.append(tokens[i]+" ");
			String resline = "bash /home/nailbiter/bin/"+
					tokens[0].substring(1)+
					".sh "+
					sb.toString();
			res.put("name", "bash");
			res.put("line", resline);
		}
		else
		{
			res.put("cmd", line);
		}
		System.out.println("botmanagerparser returns: "+res.toString());
		return res;
	}

	/* (non-Javadoc)
	 * @see util.parsers.AbstractParser#getHelpMessage()
	 */
	@Override
	public String getHelpMessage() {
		StringBuilder res = new StringBuilder();
		res.append("\tthe following commands are known:\n");
		for(int i = 0; i < help.length(); i+=2)
			res.append(""+help.getString(i) + " - "+help.getString(i+1)+"\n");
		return res.toString();
	}
}
