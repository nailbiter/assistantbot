import java.io.File;

import org.json.JSONObject;

import jshell.JShell;
import util.KeyRing;
import util.Util;

public class JShellManager implements util.MyManager{
	protected JShell shell = null;
	protected boolean isLocked = true;
	MyAssistantBot bot_;
	java.io.ByteArrayOutputStream myByteStream = new java.io.ByteArrayOutputStream();
	public JShellManager (MyAssistantBot bot) throws Exception
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
		System.out.println(String.format("res=%s in %s", res.toString(),this.getClass().getName()));
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
			return execute(res.getString("cmd"));
		}
		if(res.has("name"))
		{
			System.out.println("got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("login")==0)
				return (this.unLock(res.getString("passwd")) ? "still locked" : "unlocked");
			if(isLocked)
				return "log in first";
			if(res.getString("name").compareTo("bm")==0)
			{
				//TODO
				String botmanagerShPath = "/home/nailbiter/bin/botmanager.sh";
				String gonnaExecute = "sh "+botmanagerShPath + " " + res.getString("arguments");
				System.out.println("gonna execute: "+gonnaExecute);
				return this.execute(gonnaExecute);
			}
		}
		return null;
	}
	protected String execute(String what) throws Exception
	{
		shell.runCommand(what);
		String out = this.myByteStream.toString();
		this.myByteStream.reset();
		if(out==null||out.length()==0)
			out = "null";
		System.out.println("out="+out+", len="+out.length());
		return out;
	}
	@Override
	public String gotUpdate(String data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
