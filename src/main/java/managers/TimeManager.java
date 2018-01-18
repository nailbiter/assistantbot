package managers;
import java.util.ArrayList;
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
import org.json.JSONObject;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import it.sauronsoftware.cron4j.Scheduler;
import util.LocalUtil;
import util.MyBasicBot;
import util.MyManager;
import util.StorageManager;

/**
 * 
 */

/**
 * @author nailbiter
 *
 */
public class TimeManager implements MyManager,Runnable {
	Scheduler scheduler_;
	Long chatID_;
	MyBasicBot bot_;
	ArrayList<List<InlineKeyboardButton>> buttons = null;
	JSONArray categories = null;
	protected static int ROWNUM = 2;
	JSONArray time = null;
	boolean isWaitingForAnswer;
	SleepManager sleepManager_ = null;
	/* (non-Javadoc)
	 * @see util.MyManager#getResultAndFormat(org.json.JSONObject)
	 */
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("timestat")==0)
			{
				int num = res.optInt("num",24);
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
				for(int i = 0; i < list.size(); i++)
				{
					tb.newRow();
					tb.addToken(list.get(i).getKey()+":");
					tb.addToken(StringUtils.repeat("*",list.get(i).getValue()));
				}
				
				return tb.toString();
			}
		}
		return null;
	}
	public TimeManager(Long chatID,MyBasicBot bot,Scheduler scheduler_in, SleepManager sm) {
		this.scheduler_ = scheduler_in;
		this.chatID_ = chatID;
		this.bot_ = bot;
		this.isWaitingForAnswer = false;
		this.sleepManager_ = sm;
		makeButtons();
		scheduler_.schedule("*/30 * * * *",this);
		JSONObject timeObj = StorageManager.get("time", true);
		if(!timeObj.has("arr"))
			timeObj.put("arr", new JSONArray());
		time = timeObj.getJSONArray("arr");
	}
	protected void makeButtons()
	{
		categories = new JSONArray()
				.put("сон")
				.put("japanese")
				.put("логистика")
				.put("gym")
				.put("reading")
				.put("работа")
				.put("отдых")
				.put("общение")
				.put("без дела")
				.put("german")
				.put("coding")
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
	@Override
	public void run(){
		if(this.isWaitingForAnswer)
		{
			try{gotUpdate(categories.getString(0));}
			catch(Exception e) { e.printStackTrace(System.out); }
		}
		System.out.println("run this");
		bot_.sendMessageWithKeyBoard("where are you now?", chatID_, this, buttons);
		this.isWaitingForAnswer = true;
	}
	@Override
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
		return new JSONArray("[{\"name\":\"timestat\",\"args\":[{\"name\":\"num\",\"type\":\"int\",\"isOpt\":true}],\"help\":\"statistics about time used\"}]");
	}
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
}
