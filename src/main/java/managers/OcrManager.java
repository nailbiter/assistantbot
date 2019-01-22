package managers;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import util.AssistantBotException;
import util.Util;
import util.parsers.ParseOrdered.ArgTypes;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class OcrManager extends AbstractManager {

	private static final String TESSDATA = "src/main/resources/tessdata";
	private ResourceProvider rp_;
//	private static final String DEFLANG = "chi_tra";
	public OcrManager(ResourceProvider rp) throws AssistantBotException {
		super(GetCommands());
		rp_ = rp;
	}
	static JSONArray GetCommands() throws AssistantBotException {
		return new JSONArray()
				.put(new ParseOrderedCmd("photo","ocr on photo"
						,new ParseOrderedArg("filename",ArgTypes.string))
						.makeDefaultPhotoHandler());
	}
	public String photo(JSONObject obj) throws Exception {
//		Util.ExecuteCommand(command)
		return Util.RunScript(String.format("tesseract -l %s %s stdout"
				, getParamObject(rp_).getString("lang")
				,obj.getString("filename")));
	}
}
