package managers.tests;

import java.util.Timer;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;

import managers.MyManager;
import managers.Replier;
import managers.TestManager;
import util.MyBasicBot;

public class ParadigmTest{
	private Logger logger_ = Logger.getLogger(this.getClass().getName());
	private static final String CORNERSTONE = "--";
	MongoCollection<Document> tests_;
	
	public ParadigmTest(MyBasicBot bot_) {
		tests_ = bot_.getMongoClient().getDatabase("logistics").getCollection("paradigmTests");
	}
	public int getSize() 
	{
		return (int)tests_.count();
	}
	public String[] isCalled(int count) 
	{
		if(getSize() >= count) {
			return new String[] {String.format("paradigm test: %s", 
					(String)tests_.find(new Document("id",count)).first().get("name"))};
		}
		else
			return null;
	}
	public String getTestName(int count)
	{
		return isCalled(count)[0];
	}
	public int getColNum(int count) 
	{
		return new JSONObject(tests_.find(new Document("id",count)).first().toJson()).getJSONArray("topMostRow").length();
	}
	public int getRowNum(int count) 
	{
		return new JSONObject(tests_.find(new Document("id",count)).first().toJson()).getJSONArray("leftMostColumn").length();
	}
	public String processReply(String reply, int count) 
	{
		JSONObject obj = new JSONObject(tests_.find(new Document("id",count)).first().toJson());
		System.out.println(String.format("paradigm: processReply(count=%d,obj_=%s)",count,obj.toString()));
		return this.verify(reply,
				obj.getJSONArray("data"),
				obj.getJSONArray("topMostRow"),
				obj.getJSONArray("leftMostColumn"));
	}
	public String showTest(int count) {
		JSONObject obj = new JSONObject(tests_.find(new Document("id",count)).first().toJson());
		JSONArray col = obj.getJSONArray("leftMostColumn"),
				row = obj.getJSONArray("topMostRow"),
				answer = obj.getJSONArray("data");
		for(int i = col.length()-1; i>=0;i--)
			col.put(i+1,col.getString(i));;
		col.put(0,CORNERSTONE);
		int colNum = row.length(),
				rowNum = col.length() - 1;
		logger_.info(String.format("colnum=%d, rownum=%d", colNum,rowNum));
		util.TableBuilder tb = new util.TableBuilder();
		for(int i = 0; i < rowNum ; i++)
		{
			tb.newRow();
			for(int j = 0; j < colNum; j++)
			{
				tb.addToken(answer.getString(rowNum*j+i));
			}
		}
		logger_.info(String.format("tb=\n%s", tb.toString()));
		
		tb
			.addRow(row, 0)
			.addCol(col, 0);
		return tb.toString();
	}
	private String verify(String reply,JSONArray answer,JSONArray row, JSONArray col)
	{
		for(int i = col.length()-1; i>=0;i--)
			col.put(i+1,col.getString(i));;
		col.put(0,CORNERSTONE);
		int colNum = row.length(),
				rowNum = col.length() - 1;
		logger_.info(String.format("colnum=%d, rownum=%d", colNum,rowNum));
		boolean isCorrect = true;
		util.TableBuilder tb = new util.TableBuilder();
		String[] tokens = reply.split(" ");
		int numOfCorrectAnswers = 0;
		for(int i = 0; i < rowNum ; i++)
		{
			tb.newRow();
			for(int j = 0; j < colNum; j++)
			{
				String answerS = answer.getString(rowNum*j+i),
						correct = tokens[rowNum*j+i];
				boolean isThisCorrect = answerS.equalsIgnoreCase(correct);
				if(isThisCorrect)
					numOfCorrectAnswers++;
				isCorrect = isCorrect && isThisCorrect;
				tb.addToken(isThisCorrect ? answerS : String.format("<%s>", answerS));
			}
		}
		logger_.info(String.format("tb=\n%s", tb.toString()));
		
		tb
			.addRow(row, 0)
			.addCol(col, 0);
		return tb.toString()+String.format("%d/%d", numOfCorrectAnswers,rowNum*colNum);
	}
}
