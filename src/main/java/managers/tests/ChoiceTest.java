package managers.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import managers.OptionReplier;
import managers.TestManager;

public class ChoiceTest extends Test implements OptionReplier {
	JSONArray info_ = null;
	Random rand_ = null;
	String[] answerChoices_ = null;
	IndexAndID[] map_ = null;
	private static class IndexAndID
	{
		public int index, id;
		IndexAndID() { index = id = -1;}
	}
	public ChoiceTest(JSONObject obj, JSONObject data, TestManager master, String name) throws Exception {
		super(obj, data, master, name);
		info_ = obj.getJSONArray("data");
		rand_ = new Random();
		answerChoices_ = this.makeAnswerChoices(info_);
		map_ = new IndexAndID[this.getCount()];
		for(int i = 0; i < map_.length; i++) map_[i] = new IndexAndID();
	}
	@Override
	void onRun(int index,int messageID) { map_[index].id = messageID; }
	String[] makeAnswerChoices(JSONArray arr)
	{
		Set<String> res = new HashSet<String>();
		
		for(int index = 1; index < arr.length(); )
		{
			if(res.add(arr.getString(index)))
				logger_.info(String.format("adding %s", arr.getString(index)));		
			index+=2;
		}
		
		return res.toArray(new String[] {});
	}
	@Override
	protected String[] isCalled (int count) {
		int index = 2 * rand_.nextInt(info_.length() / 2);
		logger_.info(String.format("index=%d", index));
		map_[count].index = index;
		
		String[] res = new String[this.answerChoices_.length + 1];
		res[0] = info_.getString(index);
		logger_.info(String.format("res[0]=%s", res[0]));
		for(int i = 0; i < answerChoices_.length; i++)
			res[i+1] = answerChoices_[i];
		return res;	
	}
	
	@Override
	public String optionReply(String option, Integer msgID) {
		if(this.pendingMessages_.containsKey(msgID))
		{
			String res = null;
			int index = pendingMessages_.get(msgID),
					arrayIndex = this.map_[index].index;
			String correctAnswer = info_.getString(arrayIndex + 1);
			logger_.info(String.format("correct answer=%s", correctAnswer));
			res = String.format("%b %s", correctAnswer.equals(option),
					correctAnswer);
			pendingMessages_.remove(msgID);
			return res;
		}
		return null;
	}
	
	@Override
	public String processReply(String reply, int count) {
		return null;
	}
}
