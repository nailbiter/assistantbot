package util.parsers;

import static util.parsers.StandardParserInterpreter.CMD;

import java.util.ArrayList;

import org.apache.commons.collections4.functors.IfTransformer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;

import managers.AbstractManager;
import util.AssistantBotException;

public class SimpleParser implements AbstractParser {
	private boolean isStrict_ = true;
	ArrayList<ImmutableTriple<String,String,ImmutablePair<String,Object>>>cmds_ = 
			new ArrayList<ImmutableTriple<String,String,ImmutablePair<String,Object>>>();
	public SimpleParser makeNonStrict() {
		isStrict_ = false;
		return this;
	}
	public SimpleParser addCommand(String cmdname,String help,String methodname,Object callee) {
		cmds_.add(new ImmutableTriple<String,String,ImmutablePair<String,Object>>(cmdname,help
				,new ImmutablePair<String,Object>(methodname,callee)));
		return this;
	}
	public SimpleParser addCommand(String cmdname,String help,Object callee) {
		return addCommand(cmdname,help,cmdname,callee);
	}
	@Override
	public JSONObject parse(String line) throws Exception {
		if(line==null)
			throw new AssistantBotException(AssistantBotException.Type.SIMPLEPARSER
					,"line==null");
		String[] split = line.split(ParseOrdered.SPLITPAT,2);
		if( split.length == 0 )
			throw new AssistantBotException(AssistantBotException.Type.SIMPLEPARSER
					,String.format("split.length==0 for line=\"%s\"",line));
		for(ImmutableTriple<String, String, ImmutablePair<String, Object>> t:cmds_) {
			if( (isStrict_ && t.left.equals(split[0])) 
					|| (!isStrict_ && t.left.startsWith(split[0]))) {
				Object callee = t.right.right;
				callee.getClass()
					.getMethod(t.right.left,String.class)
					.invoke(callee, (split.length==1)?"":split[1]);
				return null;
			}
		}
		if( isStrict_ )
			throw new AssistantBotException(AssistantBotException.Type.SIMPLEPARSER
					,String.format("uknown command \"%s\"", split[0]));
		return null;
	}

	@Override
	public String getHelpMessage() {
		TableBuilder tb = new TableBuilder()
				.addTokens("name_","help_");
		for(ImmutableTriple<String, String, String> t:cmds_)
			tb.addTokens(t.left,t.middle);
		
		return tb.toString();
	}

}
