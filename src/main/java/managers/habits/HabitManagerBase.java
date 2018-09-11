package managers.habits;

import java.util.Date;
import java.util.Timer;
import java.util.logging.Logger;

import assistantbot.MyAssistantUserData;
import it.sauronsoftware.cron4j.Scheduler;
import managers.habits.HabitManagerBase.HabitRunnableEnum;
import util.MyBasicBot;

abstract class HabitManagerBase {
	enum HabitRunnableEnum{
		SENDREMINDER, SETFAILURE;
	}
	MyAssistantUserData ud_ = null;
	Scheduler scheduler = null;
	MyBasicBot bot_;
	Timer timer = new Timer();
	Logger logger_ = null;
	Long chatID_;
	HabitManagerBase(Long chatID,MyBasicBot bot,Scheduler scheduler_in, MyAssistantUserData myAssistantUserData){
		logger_ = Logger.getLogger(this.getClass().getName());
		ud_ = myAssistantUserData;
		bot_ = bot;
		scheduler = scheduler_in;
		chatID_ = chatID;
	}
	void HabitRunnableDispatch(int index,HabitRunnableEnum code)
	{
		System.out.println(String.format("HabitRunnableDispatch(%d,%s)", index,code.toString()));
		if(code == HabitRunnableEnum.SENDREMINDER) {
			bot_.sendMessage(getReminderMessage(index), chatID_);
			processSetReminder(index);
		}
		if(code==HabitRunnableEnum.SETFAILURE){
			if(waitingForHabit(index))
			{
				bot_.sendMessage(getFailureMessage(index), chatID_);
				processFailure(index);
			}
		}
	}
	abstract boolean waitingForHabit(int index);
	abstract void processFailure(int index);
	abstract void processSetReminder(int index);
	abstract String getFailureMessage(int index);
	abstract String getReminderMessage(int index);
}
