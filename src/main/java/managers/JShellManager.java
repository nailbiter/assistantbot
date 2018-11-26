package managers;
import static java.util.Arrays.asList;
import org.json.JSONArray;
import org.json.JSONObject;
import jshell.JShell;
import util.KeyRing;
import util.parsers.ParseOrdered;
import util.MyBasicBot;

public class JShellManager extends AbstractManager{
	protected JShell shell = null;
	protected boolean isLocked = true;
	MyBasicBot bot_;
	java.io.ByteArrayOutputStream myByteStream = new java.io.ByteArrayOutputStream();
	public JShellManager (MyBasicBot bot) throws Exception
	{
		super(GetCommands());
		shell = JShell.create();
		jshell.Command.setCustomOut(myByteStream);
		bot_ = bot;
	}
	protected boolean unLock(String pwd) {
		System.out.println("got passwd: "+pwd+"-"+pwd.length());
		System.out.println("should be "+KeyRing.getPasswd()+"-"+KeyRing.getPasswd().length());
		isLocked = !(KeyRing.getPasswd().compareTo(pwd)==0);
		return isLocked;
	}
	public String cmd(JSONObject res) throws Exception{
		if(isLocked)
			return "log in first";
		System.out.println("got cmd: "+res.getString("command"));
		shell.runCommand(res.getString("command"));
		String out = this.myByteStream.toString();
		this.myByteStream.reset();
		if(out==null||out.length()==0)
			out = "null";
		System.out.println("out="+out+", len="+out.length());
		return out;
	}
	public String login(JSONObject res) throws Exception{
		return (this.unLock(res.getString("passwd")) ? "still locked" : "unlocked");
	}
	public static JSONArray GetCommands() {
		return new JSONArray()
				.put(ParseOrdered.MakeCommand("login", "login into shell",
						asList(
								ParseOrdered.MakeCommandArg("passwd",ParseOrdered.ArgTypes.string,false)
								)))
				.put(ParseOrdered.MakeCommand("cmd","execute command",asList(ParseOrdered.MakeCommandArg("command",ParseOrdered.ArgTypes.remainder,false))));
	}
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
}
