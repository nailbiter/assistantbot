package opts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gnu.getopt.Getopt;

public class Option {
	private char shortKey_;
	private boolean hasArgument_ = false;
	public Option(char shortKey){
		shortKey_ = shortKey;
	}
	public Option(char shortKey, boolean hasArgument){
		this(shortKey);
		this.hasArgument_ = hasArgument;
//		return opt;
	}
	static String toString(List<Option> options) {
		StringBuilder sb = new StringBuilder();
		for(Option o : options)
			sb.append(o.shortKey_+(o.hasArgument_?":":""));
		return sb.toString();
	}
	public static Map<Character,Object> processKeyArgs(String progName,String[] args,List<Option> opts){
		Getopt g = new Getopt(progName,args,Option.toString(opts));
		HashMap<Character,Object> res = new HashMap<Character,Object>();
		int c;
		while ((c = g.getopt()) != -1)
 	   	{
			boolean flag = false;
			for(Option opt : opts) {
				if(opt.shortKey_==c) {
					if(opt.hasArgument_)
						res.put(opt.shortKey_, g.getOptarg());
					else
						res.put(opt.shortKey_, true);
					flag = true;
					break;
				}
			}
			if(flag)
				continue;
			if(c=='?')
				continue;
			System.out.print("getopt() returned " + c + "\n");
 	   	}
		return res;
	}
}
