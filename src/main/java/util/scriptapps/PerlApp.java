package util.scriptapps;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.script.ScriptException;

import org.json.JSONArray;

import assistantbot.ResourceProvider;
import util.AssistantBotException;
import util.Util;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;
import util.parsers.StandardParserInterpreter;

public class PerlApp implements ScriptApp {
	private static final String SCRIPTEXTENSION = ".pl";
	private String scriptFolder_;
	private String interpreter_;
	public PerlApp(String scriptfolder, String interpreter) {
		scriptFolder_ = scriptfolder;
		interpreter_ = interpreter;
	}
	@Override
	public ArrayList<String> getCommands() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String runCommand(String line) throws Exception {
		String[] split = line.split(StandardParserInterpreter.SPLITPATTERN,2);
		if(split.length==0)
			throw new AssistantBotException(AssistantBotException.Type.SCRIPTAPPEXCEPTION, 
					String.format("cannot parse %s, split.length==0", line));
		String cmd = split[0],
				argline = (split.length>=2) ? split[1] : ""; 
		return Util.RunScript(
				String.format("%s %s/%s%s %s" 
						,interpreter_
						,scriptFolder_
						,cmd
						,SCRIPTEXTENSION
						,argline
				));
	}
	public JSONArray getCommandsObj(String cmdpref) throws Exception {
		String scriptFolder = scriptFolder_;
		JSONArray res = new JSONArray();
		File folder = new File(scriptFolder);
		File[] listOfFiles = folder.listFiles();
		for (File file:listOfFiles) {
		  if (file.isFile()) {
			String fn = file.getName();
		    System.err.println("File " + fn);
		    if(fn.endsWith(SCRIPTEXTENSION)) {
		    	String name = fn.substring(0, fn.length()-SCRIPTEXTENSION.length());
		    	//FIXME: add help
		    	res.put(new ParseOrderedCmd(cmdpref+name,"script",
		    			new ParseOrderedArg("cmdline", ParseOrdered.ArgTypes.remainder)
		    			.useDefault("")));
		    }
		  } else if (file.isDirectory()) {
		    System.out.println("Directory " + file.getName());
		  }
		}
		return res;
	}
}
