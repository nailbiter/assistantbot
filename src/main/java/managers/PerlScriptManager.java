package managers;

import static util.parsers.StandardParserInterpreter.CMD;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import util.Util;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class PerlScriptManager extends AbstractManager {
	
	private static final String SCRIPTEXTENSION = ".pl";
	private static final String CMDPREFIX = "pl";
	private ResourceProvider rp_;
	public PerlScriptManager(ResourceProvider rp) throws Exception {
		super(GetCommands(rp));
		rp_ = rp;
	}

	@Override
	public String processReply(int messageID, String msg) {
		// TODO Auto-generated method stub
		return null;
	}
	protected static JSONArray GetCommands(ResourceProvider rp) throws Exception {
		String scriptFolder = GetParamObj(rp).getString("SCRIPTDIR");
		JSONArray res = new JSONArray();
		File folder = new File(scriptFolder);
		File[] listOfFiles = folder.listFiles();
		for (File file:listOfFiles) {
		  if (file.isFile()) {
			String fn = file.getName();
		    System.err.println("File " + fn);
		    if(fn.endsWith(SCRIPTEXTENSION)) {
		    	String name = fn.substring(0, fn.length()-SCRIPTEXTENSION.length());
		    	//FIXME: add help
		    	res.put(new ParseOrderedCmd(CMDPREFIX+name,"script",
		    			Arrays.asList(new ParseOrderedArg("cmdline", ParseOrdered.ArgTypes.remainder).makeOpt().useDefault("").j())));
		    }
		  } else if (file.isDirectory()) {
		    System.out.println("Directory " + file.getName());
		  }
		}
		return res;
	}
	protected static JSONObject GetParamObj(ResourceProvider rp) throws JSONException, Exception {
		return AbstractManager.GetParamObject(rp.getMongoClient(), PerlScriptManager.class.getName());
	}
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		System.err.println(String.format("%s got: %s",this.getClass().getName(), res.toString()));
		res = po_.parse(res);
		System.err.println("dispatcher got: "+res.toString());
		return Util.RunScript(
				String.format("%s %s/%s%s %s", 
						GetParamObj(rp_).getString("INTERPRETER"),
						GetParamObj(rp_).getString("SCRIPTDIR"),
						res.getString("name").substring(CMDPREFIX.length()),
						SCRIPTEXTENSION,
						res.getString("cmdline")));
	}
}
