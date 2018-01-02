import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import it.sauronsoftware.cron4j.Scheduler;
import util.LocalUtil;
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
	MyAssistantBot bot_;
	ArrayList<List<InlineKeyboardButton>> buttons = null;
	JSONArray categories = null;
	protected static int ROWNUM = 2;
	JSONArray time = null;
	boolean isWaitingForAnswer;
	/* (non-Javadoc)
	 * @see util.MyManager#getResultAndFormat(org.json.JSONObject)
	 */
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	public TimeManager(Long chatID,MyAssistantBot bot,Scheduler scheduler_in) {
		this.scheduler_ = scheduler_in;
		this.chatID_ = chatID;
		this.bot_ = bot;
		this.isWaitingForAnswer = false;
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
				.put("math project");
		buttons = new ArrayList<List<InlineKeyboardButton>>();
		for(int i = 0; i < categories.length();)
		{
			buttons.add(new ArrayList<InlineKeyboardButton>());
			for(int j = 0; j < ROWNUM; j++)
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
		time.put((new Date().toString())+":"+data);
		this.isWaitingForAnswer = false;
		return "got: "+data;
	}
}
