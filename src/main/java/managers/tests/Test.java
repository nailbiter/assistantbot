package managers.tests;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import managers.TestManager;
import util.LocalUtil;
import util.MyManager;

abstract public class Test implements Runnable
{
	private static boolean DEBUG = false;
	protected JSONObject obj_ = null, data_ = null;
	protected Timer timer_ = null;
	protected TestManager master_ = null;
	protected String name_;
	public Test(JSONObject obj,JSONObject data,TestManager master,String name)
	{
		obj_ = obj;
		data_ = data;
		name_ = name;
		master_ = master;
		timer_ = new Timer();
		if(DEBUG) makeDates();
		schedule();
	}
	public String getName() { return name_; }
	protected static Date parseDate(String what)
	{
		Date date = new Date();
		int hours = Integer.parseInt(what.substring(0, what.indexOf(':'))),
				minutes = Integer.parseInt(what.substring(what.indexOf(':')+1));
		date.setHours(hours);
		date.setMinutes(minutes);
		return date;
	}
	protected static String writeDate(Date d){ return String.format("%d:%d", d.getHours(),d.getMinutes()); }
	protected static final String ARRAYKEY = "a";
	protected void makeDates()
	{
		//TODO
		data_.put(ARRAYKEY, new JSONArray());
		System.out.println(String.format("run schedule: %s", name_));
		int howManyTimes = obj_.getInt("count");
		Date startDate = Test.parseDate(obj_.getString("start")),
				endDate = Test.parseDate(obj_.getString("end"));
		System.out.println(String.format("got startDate=%s, endDate=%s",
				startDate.toString(),endDate.toString()));
		Long[] dates = Test.getUniform(startDate.getTime(), endDate.getTime(), howManyTimes);
		for(int i = 0; i < dates.length; i++)
		{
			Date d = new Date(dates[i]);
			data_.getJSONArray(ARRAYKEY).put(writeDate(d));
		}
	}
	protected void schedule()
	{
		Date curDate = new Date();
		JSONArray array = data_.getJSONArray(ARRAYKEY);
		
		System.out.println(String.format("curDate=%s",LocalUtil.DateToString(curDate)));
		for(int i = 0; i < array.length(); i++)
		{
			Date d = parseDate(array.getString(i));
			Long delay = d.getTime() - curDate.getTime();
			if( delay >= 0 )
			{
				System.out.println(String.format("schedule %d at %s", i,d.toString()));
				timer_.schedule(new TestReminder(i), delay);
			}
		}
	}
	protected class TestReminder extends TimerTask{
		int index_;
		TestReminder(int index) {index_ = index;}
		@Override
		public void run() { master_.makeCall(Test.this, isCalled(index_), index_); }
	}
	private static Long[] getUniform(Long l1, Long l2, int howManyTimes)
	{
		Long[] dates = new Long[howManyTimes];
		Random rand = new Random();
		long step = (l2-l1) / howManyTimes;
		System.out.println(String.format("l1=%d, l2=%d, step=%d", l1,l2,step));
		for(int i = 0; i < howManyTimes; i++)
		{
			dates[i] = l1+i*step+ (Math.abs(rand.nextLong()) % step);
			if(!((l1 <= dates[i]) && (dates[i] <= l2)))
				System.out.println("\t\tBAD!");
		}
		return dates;
	}
	/*
	 * TODO: allow it to return choice
	 * by returning Object instead of String (choice will correspond to String[] or something)
	 */
	abstract protected String isCalled(int count);
	abstract public String processReply(String reply,int count);
	@Override
	public String toString()
	{
		return obj_.toString();
	}
	@Override
	public void run() {
		makeDates();
		schedule();
	}
	public String getCronPattern()
	{
		Date startDate = Test.parseDate(obj_.getString("start"));
		String res = null;
		System.out.println("startDate: "+startDate.toString());
		if(true || DEBUG)
			res = String.format("0 %d * * *", startDate.getHours() - 1);
		else
			res = "28 13 * * *";
		
		System.out.println(String.format("%s got cron pattern %s", getName(),res));
		return res;
	}
}