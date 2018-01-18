package util.parsers;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import util.MyManager;

public class StandardParser extends AbstractParser{
	JSONArray cmds_;
	String defaultName_ = null;
	public StandardParser(JSONArray cmds)
	{
		cmds_ = cmds;
		for(int i = 0; i < cmds_.length(); i++)
			if(cmds_.get(i) instanceof String)
			{
				defaultName_ = (String)cmds_.get(i);
				System.out.println(String.format("defName=%s, idx=%d/%d", this.defaultName_,i,
						cmds_.length()));
				break;
			}
	}
	protected static JSONArray getCommands(List<MyManager> managers) throws Exception
	{
		JSONArray cmds = StandardParser.getCommandsStatic();
		for(int i = 0; i < managers.size(); i++)
		{
			JSONArray cmds_ = managers.get(i).getCommands();
			for(int j = 0; j < cmds_.length(); j++)
				cmds.put(cmds_.get(j));
		}
		System.out.println("parser got: "+cmds.toString());
		return cmds;
	}
	public StandardParser(List<MyManager> managers) throws Exception {
		this(StandardParser.getCommands(managers));
	}
	@Override
	public String getHelpMessage() {
		return this.getTelegramHelpMessage();
	}
	protected String getStandardHelpMessage()
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
	protected String getTelegramHelpMessage()
	{
		StringBuilder res = new StringBuilder();
		res.append("\tthe following commands are known:\n");
		for(int i = 0; i < cmds_.length(); i++)
		{
			JSONObject cmd = cmds_.optJSONObject(i); 
			if(cmd==null)
				continue;
			res.append(""+cmd.getString("name") + " - "+cmd.optString("help","(none)")+"\n");
		}
		return res.toString();
	}
	//FIXME: error handling
	@Override
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
						if(!isArgOpt(arg) || (tokens.length>=(j+2)))
							res.put(arg.getString("name"),tokens[j+1]);
						continue;
					}
					if(arg.getString("type").compareTo("int")==0)
					{
						if(!isArgOpt(arg) || (tokens.length>=(j+2)))
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
	protected static boolean isArgOpt(JSONObject arg) {return arg.optBoolean("isOpt",false);}
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("help")==0)
				return getHelpMessage();
		}
		return null;
	}
	@Override
	public String gotUpdate(String data) throws Exception {
		return null;
	}
}
