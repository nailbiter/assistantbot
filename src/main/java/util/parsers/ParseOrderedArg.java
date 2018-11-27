package util.parsers;

import org.json.JSONObject;

public class ParseOrderedArg extends JSONObject {
	private static final String USINGMEMORY = "USINGMEMORY";
	public ParseOrderedArg(String name,ParseOrdered.ArgTypes type,boolean isOpt) {
		put("name", name);
		if(isOpt) put("isOpt", isOpt);
		put("type", type.toString());
	}
	public ParseOrderedArg useMemory() {
		put(USINGMEMORY ,true);
		return this;
	}
	protected static String PrintArg(JSONObject arg)
	{
		if(IsArgOpt(arg))
			return String.format("[%s%s]", arg.getString("name").toUpperCase(),
					arg.getString("type").substring(0, 1));
		else
			return String.format("%s%s", arg.getString("name").toUpperCase(),
					arg.getString("type").substring(0, 1));
	}
	protected static boolean IsArgOpt(JSONObject arg) {
		return arg.optBoolean("isOpt",false);
	}
	public static boolean IsUsingMemory(JSONObject arg) {
		return arg.optBoolean(USINGMEMORY,false);
	}
}
