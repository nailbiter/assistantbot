package managers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import assistantbot.MyAssistantUserData;
import it.sauronsoftware.cron4j.Scheduler;
import util.LocalUtil;
import util.MyBasicBot;
import util.StorageManager;
import util.parsers.StandardParser;

/**
 * 
 */

/**
 * @author nailbiter
 *
 */
public class TimeManager extends AbstractManager implements MyManager,Runnable, OptionReplier {
	Scheduler scheduler_;
	protected boolean isSleeping;
	Long chatID_;
	MyBasicBot bot_;
	ArrayList<List<InlineKeyboardButton>> buttons = null;
	JSONArray categories = null;
	protected static int ROWNUM = 2;
	JSONArray time = null;
	boolean isWaitingForAnswer;
	MyAssistantUserData userData_ = null;
	protected static final int SLEEPINDEX = 0, NOWORKINDEX = 8;
	JSONObject obj_ = null;
	JSONArray sleepingtimes_ = null, wakingtimes_ = null;
	public String timestat(JSONObject res) {
		int num = res.optInt("num",48);
		System.out.println("got num="+num);
		Hashtable<String,Integer> ht = new Hashtable<String,Integer>();
		{
			int idx = this.time.length() - 1; 
			while(num>0 && idx >= 0)
			{
				String cat = time.getString(idx);
				cat = cat.substring(cat.lastIndexOf(":")+1);
				if(!ht.containsKey(cat))
					ht.put(cat, 0);
				int res1 = ht.get(cat);
				ht.put(cat, res1+1);
				num--; idx--;
			}
		}
		List<Map.Entry<String, Integer>> list = 
				new LinkedList<Map.Entry<String,Integer>>(ht.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String,Integer>>()
			{
				@Override
				public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
					return Integer.compare(o2.getValue(),o1.getValue());
				}
			});
		util.TableBuilder tb = new util.TableBuilder();
		for(int i = 0; i < list.size(); i++){
			tb.newRow();
			tb.addToken(list.get(i).getKey()+":");
			tb.addToken(printTime(list.get(i).getValue(),res.optString("key")));
		}
		
		return tb.toString();
	}
	protected String printTime(int num, String key)
	{
		if(key.contains("n"))
			return String.format("%.1fh", num/2.0);
		else
			return StringUtils.repeat("*",num);
	}
	public TimeManager(Long chatID,MyBasicBot bot,Scheduler scheduler_in, MyAssistantUserData myAssistantUserData) {
		this.scheduler_ = scheduler_in;
		this.chatID_ = chatID;
		this.bot_ = bot;
		this.isWaitingForAnswer = false;
		this.userData_ = myAssistantUserData;
		makeButtons();
		scheduler_.schedule("*/30 * * * *",this);
		JSONObject timeObj = StorageManager.get("time", true);
		if(!timeObj.has("arr"))
			timeObj.put("arr", new JSONArray());
		time = timeObj.getJSONArray("arr");
		isSleeping = false;
		obj_ = util.StorageManager.get("sleep", true);
		if(!obj_.has("sleepingtimes"))
			obj_.put("sleepingtimes", new JSONArray());
		if(!obj_.has("wakingtimes"))
			obj_.put("wakingtimes", new JSONArray());
		this.sleepingtimes_ = obj_.getJSONArray("sleepingtimes");
		this.wakingtimes_ = obj_.getJSONArray("wakingtimes");
	}
	protected void makeButtons()
	{
		categories = new JSONArray()
				.put("сон")			//0
				.put("japanese")		//1
				.put("логистика")	//2
				.put("gym")			//3
				.put("reading")		//4
				.put("работа")		//5
				.put("отдых")		//6
				.put("общение")		//7
				.put("без дела")		//8
				.put("german")		//9
				.put("coding")		//10
				.put("math project");
		buttons = new ArrayList<List<InlineKeyboardButton>>();
		for(int i = 0; i < categories.length();)
		{
			buttons.add(new ArrayList<InlineKeyboardButton>());
			for(int j = 0; j < ROWNUM && i < categories.length(); j++)
			{
				buttons.get(buttons.size()-1).add(new InlineKeyboardButton()
						.setText(categories.getString(i))
						.setCallbackData(categories.getString(i)));
				i++;
			}
		}
	}
	protected static final String WHEREAREYOUNOW = "北鼻，你在幹什麼？";
	int waitingMessageID = -1;
	@Override
	public void run(){
		try 
		{
			System.out.println("run this");
			
			if(this.isWaitingForAnswer)
			{
				if(isSleeping())
					gotUpdate(categories.getString(TimeManager.SLEEPINDEX));
				else
					gotUpdate(categories.getString(TimeManager.NOWORKINDEX));
			}
			else
			{
				if(isSleeping())
					gotUpdate(categories.getString(TimeManager.SLEEPINDEX));
				else
					waitingMessageID = bot_.sendMessageWithKeyBoard(WHEREAREYOUNOW, chatID_, buttons);
				this.isWaitingForAnswer = true;
			}
		}
		catch(Exception e) { e.printStackTrace(System.out); }
	}
	public String gotUpdate(String data) throws Exception {
		time.put(String.format("%s:%s", LocalUtil.DateToString(new Date()),data));
		this.isWaitingForAnswer = false;
		return "got: "+data+"\n"+this.getLifetime();
	}
	protected String getLifetime()
	{
		Date currentData = new Date();
		Date myDeathData = new Date(1991 + 80, 12, 24);
		return "remaining time to live: " + LocalUtil.milisToTimeFormat(myDeathData.getTime() - currentData.getTime());
	}
	@Override
	public JSONArray getCommands() {
		JSONArray res = new JSONArray();
		res.put(AbstractManager.makeCommand("timestat", "statistics about time used", 
				Arrays.asList(AbstractManager.makeCommandArg("num", StandardParser.ArgTypes.integer, true),
						AbstractManager.makeCommandArg("key", StandardParser.ArgTypes.string, true)
						)));
		res.put(makeCommand("sleepstart","start sleeping",new ArrayList<JSONObject>()));
		res.put(makeCommand("sleepend","end sleeping",new ArrayList<JSONObject>()));
		return res;
	}
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
	@Override
	public String optionReply(String option, Integer msgID) {
		try {
			if(this.isWaitingForAnswer && this.waitingMessageID==msgID)
				return this.gotUpdate(option);
			else
			{
				System.out.format("wfa=%s, id: %d vs %d",this.isWaitingForAnswer ? "true":"false", 
						this.waitingMessageID,msgID);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return null;
		}
	}
	public boolean isSleeping(){ return this.isSleeping; }
	public String sleepstart(JSONObject obj)
	{
		this.isSleeping = true;
		this.sleepingtimes_.put((new Date()).getTime());
		return "start sleeping";
	}
	public String sleepend(JSONObject obj)
	{
		this.isSleeping = false;
		this.wakingtimes_.put((new Date()).getTime());
		if(this.isWaitingForAnswer)
		{
			try { gotUpdate(categories.getString(TimeManager.SLEEPINDEX)); }
			catch (Exception e) {
				e.printStackTrace(System.out);
				return "cannot gotUpdate";
			}
		}
		return String.format("you have slept for: %s", LocalUtil.milisToTimeFormat(
				this.wakingtimes_.getLong(this.wakingtimes_.length() - 1) - 
				this.sleepingtimes_.getLong(this.sleepingtimes_.length() - 1)));
	}
}
