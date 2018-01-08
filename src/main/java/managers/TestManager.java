/**
 * 
 */
package managers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import it.sauronsoftware.cron4j.Scheduler;
import managers.tests.ParadigmTest;
import managers.tests.PluralTest;
import managers.tests.Test;
import util.MyBasicBot;
import util.StorageManager;

/**
 * @author nailbiter
 *
 */
public class TestManager extends AbstractManager {
	Long chatID_ = null;
	Scheduler scheduler_ = null;
	MyBasicBot bot_ = null;
	List<Test> tests = null;
	public TestManager(Long chatID, MyBasicBot bot, Scheduler scheduler) {
		chatID_ = chatID;
		bot_ = bot;
		scheduler_ = scheduler;
		String name = "tests";
		JSONObject obj = StorageManager.get(name, true);
		/*if(!obj.has(name))
			obj.put(name, new JSONArray());
		jsonarray = obj.getJSONArray(name);*/
		/*for(int i = 0; i < jsonarray.length(); i++)
			tests.add(new Test(jsonarray.getJSONObject(i),i));*/
		tests = new ArrayList<Test>();
		tests.add(new ParadigmTest(obj.getJSONObject("paradigm")));
		//tests.add(new PluralTest(obj.getJSONObject("plural")));
	}

	/* (non-Javadoc)
	 * @see util.MyManager#getCommands()
	 */
	@Override
	public JSONArray getCommands() {
		// TODO Auto-generated method stub
		JSONArray res = new JSONArray();
		
		return res;
	}

}
