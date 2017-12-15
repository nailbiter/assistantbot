package jshell.command;

import jshell.Command;
import util.Util;

public class sh extends Command {

	@Override
	public void execute(String[] args) throws Exception {
		//Runtime.getRuntime().exec("sh ./"+args[0]);
		String line = "sh "+args[0];
		System.out.println("sh: "+line);
		out().println(Util.runScript(line));
	}
}