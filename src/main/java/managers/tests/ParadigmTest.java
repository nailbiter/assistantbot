package managers.tests;

import java.util.Timer;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import managers.MyManager;
import managers.Replier;
import managers.TestManager;

public class ParadigmTest{
	JSONObject obj_;
	private Logger logger_ = Logger.getLogger(this.getClass().getName());
	public ParadigmTest(JSONObject obj,JSONObject data,TestManager master,String name,Timer t) throws Exception {
		obj_ = obj;
	}
	public String toString()
	{
		return String.format("start=%s, end=%s, count=%d", 
				obj_.getString("start"),
				obj_.getString("end"),
				obj_.getInt("count"));
	}
	public int getSize() {
		return obj_.getJSONArray("data").length();
	}
	public String[] isCalled(int count) {
		/*if(count == 0)
			return new String[] {"test: the"};
		if(count == 1)
			return new String[] {"test: welch"};
		if(count == 2)
			return new String[] {"test: a"};
		if(count == 3)
			return new String[] {"test: mein"};*/
		if(obj_.getJSONArray("data").length() >= count)
			return new String[] {String.format("paradigm test: %s", 
					obj_.getJSONArray("data").getJSONArray(count).getString(3))};
		else
			return null;
	}
	public String processReply(String reply, int count) {
		System.out.println(String.format("paradigm: processReply(count=%d,obj_=%s)",count,obj_.toString()));
		return this.verify(reply,
				obj_.getJSONArray("data").getJSONArray(count).getJSONArray(0),
				obj_.getJSONArray("data").getJSONArray(count).getJSONArray(1),
				obj_.getJSONArray("data").getJSONArray(count).getJSONArray(2));
	}
	private String verify(String reply,JSONArray answer,JSONArray row, JSONArray col)
	{
		int colNum = row.length(),
				rowNum = col.length() - 1;
		logger_.info(String.format("colnum=%d, rownum=%d", colNum,rowNum));
		boolean isCorrect = true;
		util.TableBuilder tb = new util.TableBuilder();
		String[] tokens = reply.split(" ");
		for(int i = 0; i < rowNum ; i++)
		{
			tb.newRow();
			for(int j = 0; j < colNum; j++)
			{
				String answerS = answer.getString(rowNum*j+i),
						correct = tokens[rowNum*j+i];
				boolean isThisCorrect = answerS.equalsIgnoreCase(correct);
				isCorrect = isCorrect && isThisCorrect;
				tb.addToken(isThisCorrect ? answerS : String.format("<%s>", answerS));
			}
		}
		logger_.info(String.format("tb=\n%s", tb.toString()));
		
		tb
			.addRow(row, 0)
			.addCol(col, 0);
		return tb.toString();
	}
}
