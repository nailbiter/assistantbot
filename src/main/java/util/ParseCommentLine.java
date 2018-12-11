package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;

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
	 * @throws AssistantBotException 
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
					res.put(DATE, sdf_.parse(split[0].substring(DATEPREF.length())));
				} catch (ParseException e) {
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
