import org.json.JSONObject;
import jshell.JShell;
import util.KeyRing;

public class MyAssistantUserData implements UserData {
	JShell shell = null;
	private boolean locked = true;
	private HabitManager habitManager = null;
	private MoneyManager moneyManager = null;
	boolean isLocked() { return locked;}
	HabitManager getHabitManager() { return habitManager; }
	MoneyManager getMoneyManager() { return moneyManager; }
	MyAssistantUserData(Long chatID,MyAssistantBot bot){
		moneyManager = new MoneyManager(bot);
		habitManager = new HabitManager(chatID,bot);
		try {
			shell = JShell.create(); //FIXME: Security manager makes problems
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
	public void Update(JSONObject res)  {
		if(res.has("name"))
		{
			System.out.println("got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("login")==0)
			{
				System.out.println("got passwd: "+res.getString("passwd")+"-"+res.getString("passwd").length());
				System.out.println("should be "+KeyRing.getPasswd()+"-"+KeyRing.getPasswd().length());
				locked = !(KeyRing.getPasswd().compareTo(res.getString("passwd"))==0);
				System.out.println("locked="+locked);
			}
			return;
		}
		if(!res.has("cmd") || locked)
			return;
		System.out.println("got cmd: "+res.getString("cmd"));
		try {
			shell.runCommand(res.getString("cmd"));
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}

}
