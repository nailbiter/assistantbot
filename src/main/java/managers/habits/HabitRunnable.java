package managers.habits;

import java.util.TimerTask;

import managers.HabitManager;
import managers.habits.HabitManagerBase.HabitRunnableEnum;

class HabitRunnable extends TimerTask
{
//	int index_;
	String name_;
	HabitRunnableEnum code_;
	HabitManager hm_;
	HabitRunnable(String name,HabitRunnableEnum code, HabitManager hm){ 
		name_=name;
		code_ = code;
		hm_ = hm;
	}
	@Override
	public void run() { 
		hm_.HabitRunnableDispatch(name_,code_); 
	}
}