package managers.habits;

import java.util.TimerTask;

import managers.habits.HabitManagerBase.HabitRunnableEnum;

class HabitRunnable extends TimerTask
{
	int index_;
	HabitRunnableEnum code_;
	HabitManager hm_;
	HabitRunnable(int index,HabitRunnableEnum code, HabitManager hm){ index_ = index; code_ = code; hm_ = hm;}
	@Override
	public void run() { hm_.HabitRunnableDispatch(index_,code_); }
}