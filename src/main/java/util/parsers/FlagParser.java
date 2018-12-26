package util.parsers;

import java.util.HashMap;
import java.util.HashSet;

import com.github.nailbiter.util.TableBuilder;

import util.AssistantBotException;

public class FlagParser {
	HashMap<Character,String> flagDescriptions_ = new HashMap<Character,String>();
	HashSet<Character> flags_ = new HashSet<Character>();
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
		for(int i = 0; i < flagline.length(); i++) {
			char c = flagline.charAt(i);
			if(flagDescriptions_.containsKey(c)) {
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
