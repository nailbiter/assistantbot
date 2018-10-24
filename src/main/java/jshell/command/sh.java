package jshell.command;

import jshell.Command;
import util.LocalUtil;

public class sh extends Command {

	@Override
	public void execute(String[] args) throws Exception {
		//Runtime.getRuntime().exec("sh ./"+args[0]);
		System.out.println("here with "+args);
		StringBuilder sb = new StringBuilder();
		if(!args[0].startsWith("/"))
			sb.append(property("jshell.dir")+"/");
		sb.append(args[0]);
		for(int i = 1; i< args.length; i++)
			sb.append(" "+args[i]);
		String line = "sh "+sb;
		System.out.println("sh: "+line);
		out().println(LocalUtil.runScript(line));
	}
}