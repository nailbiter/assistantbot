/**
 * 
 */
package managers;

import java.util.ArrayList;
import java.util.Hashtable;
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
		tests = new ArrayList<Test>();
		tests.add(new ParadigmTest(obj.getJSONObject("paradigm"),this));
		//tests.add(new PluralTest(obj.getJSONObject("plural")));
		schedule();
	}

	private void schedule() {
		for(int i = 0; i < tests.size(); i++)
		{
			scheduler_.schedule(tests.get(i).getCronPattern(), tests.get(i));
		}
	}

	/* (non-Javadoc)
	 * @see util.MyManager#getCommands()
	 */
	@Override
	public JSONArray getCommands() {
		JSONArray res = new JSONArray();
		res.put(AbstractManager.makeCommand("tests","show tests",new ArrayList<JSONObject>()));
		return res;
	}

	@Override
	public String processReply(int messageID,String msg) {
		System.out.println(String.format("messageID=%d", messageID));
		TestAndIndex tai = waitingForReply.get(messageID);
		return tai.test.processReply(msg, tai.index);
	}
	Hashtable<Integer,TestAndIndex> waitingForReply = new Hashtable<Integer,TestAndIndex>();
	private static class TestAndIndex
	{
		Test test;
		int index;
		TestAndIndex(Test t, int i){ test = t; index = i; }
	}
	public void makeCall(Test whom, String text, int index)
	{
		try {
			int msgID = bot_.sendMessage(text, chatID_, this);
			System.out.println(String.format("msgID=%d, index=%d", msgID,index));
			waitingForReply.put(msgID, new TestAndIndex(whom,index));
		} catch (Exception e) {
			System.out.println("cannot makeCall");
			e.printStackTrace();
		}
	}
	public String tests(JSONObject obj)
	{
		util.TableBuilder tb = new util.TableBuilder().addNewlineAndTokens("name", "description");
		
		for(int i = 0; i < tests.size(); i++)
		{
			tb
				.newRow()
				.addToken(tests.get(i).getName())
				.addToken(tests.get(i).toString());
		}
		return tb.toString();
	}
}
