package util.parsers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import com.github.nailbiter.util.TableBuilder;

import util.AssistantBotException;

public class FlagParser {
	HashMap<Character,String> flagDescriptions_ = new HashMap<Character,String>();
	HashSet<Character> flags_ = new HashSet<Character>();
	Logger logger_ = Logger.getLogger(FlagParser.class.getName());
	boolean isStrict_ = false;
	public FlagParser addFlag(char f,String description) throws AssistantBotException {
		if(f == 'h')
			throw new AssistantBotException(AssistantBotException.Type.FLAGPARSEREXCEPTION,"cannot use 'h'");
		if(flagDescriptions_.containsKey(f))
			throw new AssistantBotException(AssistantBotException.Type.FLAGPARSEREXCEPTION,
					String.format("already has key '%s': \"%s\"", 
							new Character(f).toString(),flagDescriptions_.get(f)));
		flagDescriptions_.put(f, description);
		return this;
	}
	public FlagParser makeStrict() {
		isStrict_ = true;
		return this;
	}
	public String getHelp() {
		TableBuilder tb = new TableBuilder();
		tb.addTokens("flag_","description_");
		for(Character c:flagDescriptions_.keySet()) {
			tb.addTokens(c.toString(),flagDescriptions_.get(c));
		}
		tb.addTokens("h","show this message");
		return tb.toString();
	}
	public FlagParser parse(String flagline) throws AssistantBotException {
		flags_.clear();
		logger_.info("going to parse: "+flagline);
		for(int i = 0; i < flagline.length(); i++) {
			logger_.info("\tgoing to parse: "+new Character(flagline.charAt(i)).toString());
			char c = flagline.charAt(i);
			if(flagDescriptions_.containsKey(c)) {
				logger_.info("\tadding");
				flags_.add(c);
			} else if( isStrict_ ) {
				throw new AssistantBotException(
						AssistantBotException.Type.FLAGPARSEREXCEPTION,String.format("unknown flag: '%s'", new Character(c).toString()));
			}
		}
		return this;
	}
	public boolean contains(char c) {
		return flags_.contains(c);
	}
}
