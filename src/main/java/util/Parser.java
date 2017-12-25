package util;

import org.json.JSONArray;
import org.json.JSONObject;

public class Parser {
	JSONArray cmds_;
	public Parser(JSONArray cmds)
	{
		cmds_ = cmds;
	}
	public String getHelpMessage()
	{
		StringBuilder res = new StringBuilder();
		res.append("the following commands are known:\n");
		for(int i = 0; i < cmds_.length(); i++)
		{
			JSONObject cmd = cmds_.optJSONObject(i); 
			if(cmd==null)
				continue;
			res.append("\t/"+cmd.getString("name") + " : "+cmd.optString("help","")+"\n");
		}
		return res.toString();
	}
	//FIXME: error handling
	public JSONObject parse(String line) throws Exception
	{
		String[] tokens = line.split(" ");
		for(int i = 0; i < ( cmds_.length() - 1 ); i++)
		{
			if(tokens[0].compareTo("/"+cmds_.getJSONObject(i).getString("name"))==0)
			{
				JSONArray args = cmds_.getJSONObject(i).getJSONArray("args");
				JSONObject res = new JSONObject().put("name", cmds_.getJSONObject(i).getString("name"));
				for(int j = 0; j < args.length();j++)
				{
					JSONObject arg = args.getJSONObject(j);
					if(arg.getString("type").compareTo("string")==0)
					{
						res.put(arg.getString("name"),tokens[j+1]);
						continue;
					}
					if(arg.getString("type").compareTo("int")==0)
					{
						res.put(arg.getString("name"),Integer.parseInt(tokens[j+1]));
						continue;
					}
					if(arg.getString("type").compareTo("remainder")==0)
					{
						StringBuilder sb = new StringBuilder(tokens[j+1]);
						/*FIXME: the next snippet may cause troubles if
						 *  tokens are separated by several whitespaces
						 *  and this needed to be preserved
						 */ 
						for(int k = j+2; k < tokens.length; k++)
							sb.append(" "+tokens[k]);
						res.put(arg.getString("name"),sb.toString());
						break;
					}
					throw new Exception("unknown type: "+arg.optString("type"));
				}
				return res;
			}
		}
		
		return new JSONObject().put(cmds_.getString(cmds_.length()-1), line);
	}
}
