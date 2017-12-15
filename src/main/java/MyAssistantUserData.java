import org.json.JSONObject;
import jshell.JShell;

public class MyAssistantUserData implements UserData {
	JShell shell = null;
	MyAssistantUserData(){
		try {
			shell = JShell.create(); //FIXME: Security manager makes problems
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
	public void Update(JSONObject res) {
		// TODO Auto-generated method stub
		if(!res.has("cmd"))
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
