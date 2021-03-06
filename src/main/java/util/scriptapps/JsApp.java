package util.scriptapps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import util.scripthelpers.ScriptHelper;

public class JsApp implements ScriptApp {
	private String scriptFolder_;
	private ScriptEngineManager factory;
	private static final String SCRIPTHELPERVARNAME = "ScriptHelper";
	private static final String SCRIPTEXTENSION = ".js";
	private String includeFolderName_;
	private ScriptHelper sh_;

	public JsApp(String scriptFolder, ScriptHelper sh) {
		this(scriptFolder,"common",sh);
	}
	public JsApp(String scriptFolder, String includeFolderName,ScriptHelper sh) {
		sh_ = sh;
		includeFolderName_ = includeFolderName;
		
		scriptFolder_ = scriptFolder; 
		if(!scriptFolder_.endsWith("/"))
			scriptFolder_ += "/";
		System.err.format("script folder: %s\n", scriptFolder_);
		
		System.err.format("commands: %s\n", getCommands().toString());
        factory = new ScriptEngineManager();
	}
	/* (non-Javadoc)
	 * @see util.scriptapps.ScriptApp#getCommands()
	 */
	@Override
	public ArrayList<String> getCommands(){
		ArrayList<String> commands = new ArrayList<String>();
		PopulateCommands(commands,scriptFolder_);
		return commands;
	}
	private static void PopulateCommands(ArrayList<String> commands, String scriptFolder) {
		File folder = new File(scriptFolder);
		File[] listOfFiles = folder.listFiles();
		for (File file:listOfFiles) {
		  if (file.isFile()) {
			String fn = file.getName();
		    System.err.println("File " + fn);
		    if(fn.endsWith(".js")) {
		    	commands.add(fn.substring(0, fn.length()-3));
		    }
		  } else if (file.isDirectory()) {
		    System.err.println("Directory " + file.getName());
		  }
		}
	}
	/* (non-Javadoc)
	 * @see util.scriptapps.ScriptApp#runCommand(java.lang.String)
	 */
	@Override
	public String runCommand(String line) throws FileNotFoundException, ScriptException, NoSuchMethodException {
    	String[] split = line.split(" ",2);
    	String path = scriptFolder_ + split[0]+SCRIPTEXTENSION;
    	System.err.format("path: %s\n", path);
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		Invocable inv = (Invocable) engine;
		sh_.setInvocable(inv);
        engine.put(SCRIPTHELPERVARNAME, sh_);
		preloadEngine(engine,scriptFolder_);
		engine.eval(new java.io.FileReader(path));
		return (String) inv.invokeFunction("main",(split.length>1)?split[1]:null);
	}
	private void preloadEngine(ScriptEngine engine, String scriptFolder) throws FileNotFoundException, ScriptException {
		if(includeFolderName_ == null )
			return;
		
		String includeFolder = String.format("%s%s/", scriptFolder,includeFolderName_);
		System.err.format("incFol=%s\n", includeFolder);
		
		File incFolder = new File(includeFolder);
		File[] listOfFiles = incFolder.listFiles();
		for (File file:listOfFiles) {
		  if (file.isFile()) {
			String fn = file.getName();
		    if(fn.endsWith(SCRIPTEXTENSION)) {
		    	System.err.format("\tfound \"%s\" to preload\n", fn);
		    	engine.eval(new FileReader(file.getAbsolutePath()));
		    }
		  } else if (file.isDirectory()) {
		  }
		}
	}
	public <T> T getInterface(Class<T> clasz,String fn) throws FileNotFoundException, ScriptException{
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		Invocable inv = (Invocable) engine;
		sh_.setInvocable(inv);
        engine.put(SCRIPTHELPERVARNAME, sh_);
		preloadEngine(engine,scriptFolder_);
		String path = scriptFolder_ + fn + SCRIPTEXTENSION;
		engine.eval(new java.io.FileReader(path));
		return inv.getInterface(clasz);
	}
}
