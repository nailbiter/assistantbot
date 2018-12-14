package util.parsers;

import org.apache.commons.collections4.Transformer;
import org.json.JSONObject;

public class ParseOrderedArg extends JSONObject {
	private static final String USINGMEMORY = "_USINGMEMORY";
	private static final String USINGDEFAULT = "_USINGDEFAULT";
	public ParseOrderedArg(String name,ParseOrdered.ArgTypes type) {
		put("name", name);
		put("type", type.toString());
	}
	/**
	 * @deprecated
	 * @param name
	 * @param type
	 * @param isOpt
	 */
	public ParseOrderedArg(String name,ParseOrdered.ArgTypes type,boolean isOpt) {
		put("name", name);
		if(isOpt) put("isOpt", isOpt);
		put("type", type.toString());
	}
	public ParseOrderedArg useMemory(){
//		if(!IsArgOpt(this))
//			throw new Exception(String.format("cannot %s on non-opt argument!", "useMemory"));
		makeOpt();
		put(USINGMEMORY ,true);
		return this;
	}
	public ParseOrderedArg useMemory(Transformer<Object,Object> t){
//		if(!IsArgOpt(this))
//			throw new Exception(String.format("cannot %s on non-opt argument!", "useMemory"));
		makeOpt();
		put(USINGMEMORY ,t);
		return this;
	}
	public ParseOrderedArg makeOpt() {
		put("isOpt",true);
		return this;
	}
	public ParseOrderedArg useDefault(Object defValue){
		makeOpt();
//		if(!IsArgOpt(this))
//			throw new Exception(String.format("cannot %s on non-opt argument!", "useDefault"));
		put(USINGDEFAULT,defValue);
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
	public static Transformer<Object,Object> GetMemoryTransformer(JSONObject arg) {
		Object res = arg.opt(USINGMEMORY);
		if(res != null && res instanceof Transformer<?,?>)
			return (Transformer<Object,Object>)res;
		else
			return null;
	}
	public static Object GetDefault(JSONObject arg) {
		return arg.opt(USINGDEFAULT);
	}
	public JSONObject j() {
		return (JSONObject)this;
	}
}
