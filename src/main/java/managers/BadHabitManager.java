package managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.MyAssistantUserData;
import util.LocalUtil;
import util.StorageManager;
import util.parsers.StandardParser;

public class BadHabitManager extends AbstractManager implements OptionReplier {
	JSONArray badhabits = null;
	String[] bh = {"alcohol","porn","mast","bite nails","pick nose"};
	JSONArray bhjson = new JSONArray();
	MyAssistantUserData ud_ = null;
	Set<Integer> pendingMessages = new HashSet<Integer>();
	private String listBadHabits(int count) {
		int index = this.badhabits.length() - 1;
		util.TableBuilder tb = new util.TableBuilder();
		tb.addNewlineAndTokens("what", "date");
		while(index>=0 && count>0)
		{
			tb.addNewlineAndTokens(badhabits.getJSONArray(index).getString(1),
					badhabits.getJSONArray(index).getString(0));
			count--; index--;
		}
		return tb.toString();
	}
	public String bh(JSONObject res)
	{
		this.pendingMessages.add(ud_.sendMessageWithKeyBoard("which one?",this.bhjson ));
		return "prepare to confess bad habit";
	}
	public String bhs(JSONObject res)
	{
		return this.listBadHabits(res.getInt("count"));
	}
	public BadHabitManager(MyAssistantUserData myAssistantUserData)
	{
		JSONObject obj = StorageManager.get("badhabits", true);
		if(!obj.has("a"))
			obj.put("a", new JSONArray());
		badhabits = obj.getJSONArray("a");
		ud_ = myAssistantUserData;
		for(int i = 0; i < bh.length; i++)
			this.bhjson.put(bh[i]);
	}
	@Override
	public JSONArray getCommands() {
		String pref = "bh";
		JSONArray res = new JSONArray();
		/*for(int i = 0; i < bh.length; i++)
			res.put(super.makeCommand(pref+bh[i], String.format("bad habit: %s", bh[i]), 
					new ArrayList<JSONObject>()));*/
		res.put(super.makeCommand(pref+"", "confess bad habit",new ArrayList<JSONObject>()));
		res.put(super.makeCommand(pref+"s", "watch bad habits", 
				Arrays.asList(super.makeCommandArg("count", StandardParser.ArgTypes.integer, true))));
		return res;
	}

	@Override
	public String processReply(int messageID, String msg) {
		return null;
	}
	@Override
	public String optionReply(String option, Integer msgID) {
		try 
		{
			if(this.pendingMessages.contains(msgID))
			{
				JSONArray ress = new JSONArray()
						.put(LocalUtil.DateToString(new Date()))
						.put(option);
				badhabits.put(ress);
				return ress.toString();
			}
			else
				return null;
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
			return null;
		}
	}
}
