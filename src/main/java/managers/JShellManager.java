package managers;
import static java.util.Arrays.asList;
import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import jshell.JShell;
import util.AssistantBotException;
import util.KeyRing;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;
import static util.parsers.ParseOrdered.MakeCommand;
import static util.parsers.ParseOrdered.MakeCommandArg;
import static util.parsers.ParseOrdered.ArgTypes;

public class JShellManager extends AbstractManager{
	protected JShell shell = null;
	protected boolean isLocked = true;
	java.io.ByteArrayOutputStream myByteStream = new java.io.ByteArrayOutputStream();
	public JShellManager (ResourceProvider rp) throws Exception
	{
		super(GetCommands());
		shell = JShell.Create();
		jshell.Command.setCustomOut(myByteStream);
	}
	protected boolean unLock(String pwd) {
		System.err.format("got passwd: \"%s\",\nshould be: \"%s\"", pwd,KeyRing.getPasswd());
		isLocked = !(KeyRing.getPasswd().compareTo(pwd)==0);
		return isLocked;
	}
	public String cmd(JSONObject res) throws Exception{
		if(isLocked)
			return "log in first";
		System.err.println("got cmd: "+res.getString("command"));
		shell.runCommand(res.getString("command"));
		String out = this.myByteStream.toString();
		this.myByteStream.reset();
		if(out==null||out.length()==0)
			out = "null";
		System.err.println("out="+out+", len="+out.length());
		return out;
	}
	public String login(JSONObject res) throws Exception{
		return (this.unLock(res.getString("passwd")) ? "still locked" : "unlocked");
	}
	public static JSONArray GetCommands() throws AssistantBotException {
		return new JSONArray()
				.put(new ParseOrderedCmd("login", "login into shell",
								new ParseOrderedArg("passwd",ArgTypes.string)))
				.put(
				new ParseOrderedCmd("cmd","execute command",
						new ParseOrderedArg("command",ArgTypes.remainder)
						.makeOpt())
				.makeDefaultHandler());
	}
//	@Override
//	public String processReply(int messageID,String msg) {
//		return null;
//	}
}
