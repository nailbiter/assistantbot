package managers;

import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import util.AssistantBotException;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrderedArg;
import util.parsers.ParseOrderedCmd;

public class NutritionManager extends AbstractManager {

	private ResourceProvider rp_;

	public NutritionManager(ResourceProvider rp) throws AssistantBotException {
		super(new JSONArray()
				.put(new ParseOrderedCmd("nutrition","log nutrition data"
						,new ParseOrderedArg("place",ParseOrdered.ArgTypes.string)
						,new ParseOrderedArg("id",ParseOrdered.ArgTypes.integer))));
		rp_ = rp;
	}
	public String nutrition(JSONObject arg) {
		return arg.toString(2);
	}
}
