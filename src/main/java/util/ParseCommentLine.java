package util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import util.parsers.ParseOrdered;

public class ParseCommentLine {
	public static enum Mode{
		FROMLEFT,FROMRIGHT
	}
	private Mode m_;
	private static final String PATTERN = "yyyyMMddHHmm";
	private static SimpleDateFormat SDF = new SimpleDateFormat(PATTERN);
	public final static String TAGS = "tags";
	public final static String REM = "rem";
	public final static String DATE = "date";
	private final static String DATEPREF = "%";
	private final static String TAGSPREF = "#";
	public static enum TOKENTYPE {
			STRING,DATE
	};
	private static ArrayList<ImmutableTriple<String, String, TOKENTYPE>> handlers_ = 
			new ArrayList<ImmutableTriple<String,String,TOKENTYPE>>();
	public ParseCommentLine(Mode m) {
		m_ = m;
		handlers_.add(new ImmutableTriple<String, String, TOKENTYPE>(
				TAGS,TAGSPREF,TOKENTYPE.STRING
				));
		handlers_.add(new ImmutableTriple<String, String, TOKENTYPE>(
				DATE,DATEPREF,TOKENTYPE.DATE
				));
		
	}
	public ParseCommentLine addHandler(String name,String pref,TOKENTYPE tokentype) {
		handlers_
			.add(new ImmutableTriple<String, String, TOKENTYPE>(name,pref,tokentype));
		return this;
	}
	private static void SortHandlers(ArrayList<ImmutableTriple<String, String, TOKENTYPE>> handlers) {
		Collections.sort(handlers, new Comparator<ImmutableTriple<String, String, TOKENTYPE>>(){
			@Override
			public int compare(ImmutableTriple<String, String, TOKENTYPE> o1,
					ImmutableTriple<String, String, TOKENTYPE> o2) {
				return -Integer.compare(o1.middle.length(), o2.middle.length());
			}
		});
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
		
		SortHandlers(handlers_);
		for(ImmutableTriple<String, String, TOKENTYPE> h:handlers_)
			if(h.right==TOKENTYPE.STRING)
				res.put(h.left, new HashSet<String>());
		
		for( String[] split = SplitInTwo(line,ParseOrdered.SPLITPAT,m_) ;  ; line = split[1], split = SplitInTwo(line,ParseOrdered.SPLITPAT,m_) ) {
			System.err.format("starting new iteration with line=\"%s\"\n", line);
			
			if( split.length == 0 ) {
				break;
			} else {
				boolean willBreak = true;
				for(ImmutableTriple<String, String, TOKENTYPE> h:handlers_) {
					if(split[0].startsWith(h.middle)) {
						Handle(h,split[0],res);
						willBreak = false;
						break;
					}
				}
				if( willBreak )
					break;
			}
			
			if( split.length == 1 ) {
				line = null;
				break;
			}
		}
		
		if( line != null )
			res.put(REM, line.trim());
		
		return res;
	}
	private static void Handle(ImmutableTriple<String, String, TOKENTYPE> h, String token, HashMap<String, Object> res) throws AssistantBotException {
		if( h.right == TOKENTYPE.STRING ) {
//			if( !res.containsKey(h.left) )
//				res.put(h.left, new HashSet<String>());
			((HashSet<String>)res.get(h.left))
				.add(token.substring(h.middle.length()));
		} else if( h.right == TOKENTYPE.DATE) {
			try {
				String dateline = token.substring(h.middle.length());
				Date d = null;
				if(Pattern.matches(String.format("\\d{%s}", PATTERN.length()), dateline))
					d = SDF.parse(dateline);
				else
					d = ParseCommentLine.ComputePostponeDate(dateline);
				res.put(h.left, d);
			} catch (Exception e) {
				e.printStackTrace();
				throw new AssistantBotException(AssistantBotException.Type.COMMENTPARSE, 
						String.format("cannot parse %s from \"%s\"", DATE,token));
			}
		}
	}
	private String[] SplitInTwo(String src,String pat, Mode m) throws AssistantBotException {
		if( src == null || pat == null || m == null ) {
			throw new AssistantBotException(AssistantBotException.Type.COMMENTPARSE, 
					String.format("cannot parse \"%s\" with mode %s", src,m.toString()));
		} else if( m == Mode.FROMLEFT ) {
			return src.split(pat,2);
		} else if( m == Mode.FROMRIGHT ) {
			String[] split = src.split(pat);
			if ( split.length <= 1 ) {
				return split;
			} else {
				String last = split[split.length-1];
				return new String[] {last,src.substring(0, src.lastIndexOf(last))};
			}
		} else
			throw new AssistantBotException(AssistantBotException.Type.COMMENTPARSE, 
					String.format("cannot parse \"%s\" with mode %s", src,m.toString()));			
	}
	private static Date ComputePostponeDate(String string) throws Exception {
		Matcher m = null;
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("JST"));
		
		if((m = Pattern.compile("(\\d{2})(\\d{2})(\\d{2})(\\d{2})")
				.matcher(string)).matches()) {
			c.set(Calendar.MONTH, Integer.parseInt(m.group(1))-1);
			c.set(Calendar.DATE, Integer.parseInt(m.group(2)));
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(3)));
			c.set(Calendar.MINUTE, Integer.parseInt(m.group(4)));
			return c.getTime();
		} if((m = Pattern.compile("(\\d{2})(\\d{2})(\\d{2})").matcher(string))
				.matches()) {
			c.set(Calendar.DATE, Integer.parseInt(m.group(1)));
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(2)));
			c.set(Calendar.MINUTE, Integer.parseInt(m.group(3)));
			return c.getTime();
		} if((m = Pattern.compile("\\+(\\d*)([h])").matcher(string)).matches()) {
			String unit = m.group(2);
			int num = Integer.parseInt(m.group(1));
			System.err.format("unit=%s, num=%d\n", unit,num);
			if(unit.equals("h")) {
				c.add(Calendar.HOUR, num);
			} else
				throw new Exception(String.format("cannot + parse %h", string));
			return c.getTime();
		} if((m = Pattern.compile("(\\d{2})(\\d{2})").matcher(string)).matches()) {
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(1)));
			c.set(Calendar.MINUTE, Integer.parseInt(m.group(2)));
			return c.getTime();
		} else {
			throw new Exception(String.format("cannot parse \"%s\"", string));
		}
	}
}
