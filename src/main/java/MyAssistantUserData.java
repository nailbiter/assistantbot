import org.json.JSONObject;
import jshell.JShell;
import util.KeyRing;

public class MyAssistantUserData implements UserData {
	JShell shell = null;
	boolean locked = true;
	MyAssistantUserData(){
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
				locked = !(KeyRing.getPasswd().compareTo(res.getString("passwd"))==0);
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
