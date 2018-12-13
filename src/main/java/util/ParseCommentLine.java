package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

public class ParseCommentLine {
	public static enum Mode{
		FROMLEFT,FROMRIGHT
	}
	private Mode m_;
	private SimpleDateFormat sdf_;
	public final static String TAGS = "tags";
	public final static String REM = "rem";
	public final static String DATE = "date";
	public final static String DATEPREF = "%";
	public final static String TAGSPREF = "#";
	private static final String PATTERN = "yyyyMMddHHmm";
	public ParseCommentLine(Mode m) {
		m_ = m;
		sdf_ = new SimpleDateFormat(PATTERN);
	}
	/**
	 * 
	 * @param line
	 * @param mode
	 * @return 
	 * 	.REM 	:String
	 *  .TAGS	:HashSet<String>
	 *  .DATE	:Date
	 * @throws AssistantBotException if cannot parse the date 
	 */
	public HashMap<String,Object> parse(String line) throws AssistantBotException {
		if( m_ != Mode.FROMLEFT )
			throw new AssistantBotException(AssistantBotException.Type.COMMENTPARSE, 
					String.format("cannot parse \"%s\"", line));
		
		HashMap<String, Object> res = new HashMap<String,Object>();
		res.put(TAGS, new HashSet<String>());
				
		for( String[] split = line.split(" +",2) ;  ; line = split[1], split = line.split(" +",2) ) {
			if( split.length == 0 )
				break;
			
			if( split[0].startsWith(TAGSPREF) ) {
				((HashSet<String>)res.get("tags")).add(split[0]);
			} else if( split[0].startsWith(DATEPREF) ) {
				try {
					String dateline = split[0].substring(DATEPREF.length());
					Date d = null;
					if(Pattern.matches(String.format("\\d{%s}", PATTERN.length()), dateline))
						d = sdf_.parse(dateline);
					else
						d = Util.ComputePostponeDate(dateline);
					res.put(DATE, d);
				} catch (Exception e) {
					e.printStackTrace();
					throw new AssistantBotException(AssistantBotException.Type.COMMENTPARSE, 
							String.format("cannot parse %s from \"%s\"", DATE,split[0]));
				}
			} else {
				break;
			}
			
			if( split.length == 1 ) {
				line = null;
				break;
			}
		}
		
		if( line != null )
			res.put(REM, line);
		
		return res;
	}
}
