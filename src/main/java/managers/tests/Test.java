package managers.tests;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import managers.TestManager;
import util.MyManager;

abstract public class Test implements Runnable
{
	private static boolean DEBUG = false;
	protected JSONObject obj_ = null;
	protected Timer timer_ = null;
	protected TestManager master_ = null;
	protected String name_;
	public Test(JSONObject obj,TestManager master,String name)
	{
		obj_ = obj;
		name_ = name;
		master_ = master;
		if(DEBUG) schedule();
	}
	public String getName() { return name_; }
	private static Date parseDate(String what)
	{
		Date date = new Date();
		int hours = Integer.parseInt(what.substring(0, what.indexOf(':'))),
				minutes = Integer.parseInt(what.substring(what.indexOf(':')+1));
		date.setHours(hours);
		date.setMinutes(minutes);
		return date;
	}
	protected void schedule()
	{
		System.out.println("run schedule");
		timer_ = new Timer();
		int howManyTimes = obj_.getInt("count");
		Date startDate = Test.parseDate(obj_.getString("start")),
				endDate = Test.parseDate(obj_.getString("end")),
				curDate = new Date();
		System.out.println(String.format("got startDate=%s, endDate=%s, curDate=%s",
				startDate.toString(),endDate.toString(),curDate.toString()));
		Long[] dates = Test.getUniform(startDate.getTime(), endDate.getTime(), howManyTimes);
		System.out.println("got dates:");
		for(int i = 0; i < dates.length; i++)
		{
			Date date = new Date(dates[i]);
			System.out.println("\t"+date.toString());
			Long delay = date.getTime() - curDate.getTime();
			if(delay>=0)
			{
				System.out.println(String.format("schedule %d at %s", i,date.toString()));
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