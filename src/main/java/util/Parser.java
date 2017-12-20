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
		//TODO
		return "TODO";
	}
	//FIXME: error handling
	public JSONObject parse(String line)
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
					}
				}
				return res;
			}
		}
		
		return new JSONObject().put(cmds_.getString(cmds_.length()-1), line);
	}
}
