package managers.habits;

import java.util.TimerTask;

import managers.HabitManager;
import managers.habits.HabitManagerBase.HabitRunnableEnum;

public class HabitRunnable extends TimerTask
{
	String name_;
	HabitRunnableEnum code_;
	HabitManagerBase hm_;
	public HabitRunnable(String name,HabitRunnableEnum code, HabitManagerBase hm){ 
		name_=name;
		code_ = code;
		hm_ = hm;
	}
	@Override
	public void run() { 
		try {
			hm_.habitRunnableDispatch(name_,code_);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}