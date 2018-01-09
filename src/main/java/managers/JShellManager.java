package managers;
import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import jshell.JShell;
import util.KeyRing;
import util.MyBasicBot;
import util.Util;

public class JShellManager implements util.MyManager{
	protected JShell shell = null;
	protected boolean isLocked = true;
	MyBasicBot bot_;
	java.io.ByteArrayOutputStream myByteStream = new java.io.ByteArrayOutputStream();
	public JShellManager (MyBasicBot bot) throws Exception
	{
		shell = JShell.create();
		jshell.Command.setCustomOut(myByteStream);
		bot_ = bot;
	}
	protected boolean unLock(String pwd) {
		/*
		System.out.println("got passwd: "+res.getString("passwd")+"-"+res.getString("passwd").length());
		System.out.println("should be "+KeyRing.getPasswd()+"-"+KeyRing.getPasswd().length());
		locked = !(KeyRing.getPasswd().compareTo(res.getString("passwd"))==0);
		System.out.println("locked="+locked);*/
		System.out.println("got passwd: "+pwd+"-"+pwd.length());
		System.out.println("should be "+KeyRing.getPasswd()+"-"+KeyRing.getPasswd().length());
		isLocked = !(KeyRing.getPasswd().compareTo(pwd)==0);
		return isLocked;
	}
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("filename") && !isLocked)
		{
			File file = Util.downloadPhotoByFilePath(res.getString("filepath"),bot_);
			String fn = "./"+res.getString("filename");
			File file2 = new File(fn);
			Util.copyFileUsingStream(file, file2);
			return "saved "+res.getString("filename");
		}
		if(res.has("cmd"))
		{
			if(isLocked)
				return "log in first";
			System.out.println("got cmd: "+res.getString("cmd"));
			shell.runCommand(res.getString("cmd"));
			String out = this.myByteStream.toString();
			this.myByteStream.reset();
			if(out==null||out.length()==0)
				out = "null";
			System.out.println("out="+out+", len="+out.length());
			return out;
		}
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("login")==0)
				return (this.unLock(res.getString("passwd")) ? "still locked" : "unlocked");
		}
		return null;
	}
	@Override
	public String gotUpdate(String data) throws Exception {
		return null;
	}
	@Override
	public JSONArray getCommands() {
		return new JSONArray("[{\"name\":\"login\",\"args\":[{\"name\":\"passwd\",\"type\":\"string\"}],\"help\":\"login into shell\"},\"cmd\"]");
	}
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
}
