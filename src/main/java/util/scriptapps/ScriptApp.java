package util.scriptapps;

import java.util.ArrayList;

public interface ScriptApp {
	ArrayList<String> getCommands();
	String runCommand(String line) throws Exception;
}