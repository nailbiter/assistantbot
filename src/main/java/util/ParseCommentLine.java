package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseCommentLine {
	public static enum Mode{
		FROMLEFT,FROMRIGHT
	}
	private Mode m_;
	private SimpleDateFormat sdf_;
	private final String SPLITPAT = " +";
	public final static String TAGS = "tags";
	public final static String REM = "rem";
	public final static String DATE = "date";
	private final static String DATEPREF = "%";
	private final static String TAGSPREF = "#";
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
	 * FIXME cut prefix in TAGS
	 */
	public HashMap<String,Object> parse(String line) throws AssistantBotException {
		HashMap<String, Object> res = new HashMap<String,Object>();
		res.put(TAGS, new HashSet<String>());
				
		for( String[] split = SplitInTwo(line,SPLITPAT,m_) ;  ; line = split[1], split = SplitInTwo(line,SPLITPAT,m_) ) {
			if( split.length == 0 )
				break;
			
			if( split[0].startsWith(TAGSPREF) ) {
				((HashSet<String>)res.get("tags"))
				.add(split[0].substring(TAGSPREF.length()));
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
	private String[] SplitInTwo(String src,String pat, Mode m) throws AssistantBotException {
		if( src == null || pat == null || m == null ) {
			throw new AssistantBotException(AssistantBotException.Type.COMMENTPARSE, 
					String.format("cannot parse \"%s\" with mode %s", src,m.toString()));
		} else if( m == Mode.FROMLEFT ) {
			return src.split(pat,2);
		} else if( m == Mode.FROMRIGHT ) {
			String[] split = src.split(pat);
			if( split.length <= 1 ) {
				return split;
			} else {
				String last = split[split.length-1];
				return new String[] {last,src.substring(0, src.lastIndexOf(last))};
			}
		} else
			throw new AssistantBotException(AssistantBotException.Type.COMMENTPARSE, 
					String.format("cannot parse \"%s\" with mode %s", src,m.toString()));			
	}
	//FIXME: remove
	public static String getTagspref() {
		return TAGSPREF;
	}
}
