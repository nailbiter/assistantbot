package managers.tests;

import java.util.ArrayList;
import java.util.Timer;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import managers.MyManager;
import managers.Replier;
import managers.TestManager;
import util.MyBasicBot;
import util.TableBuilder;

public class ParadigmTest extends JsonTest{
	private Logger logger_ = Logger.getLogger(this.getClass().getName());
	private static final String CORNERSTONE = "--";
	
	public ParadigmTest(JSONObject obj) {
		obj_ = obj;
	}
	public int getColNum() 
	{
		return obj_.getJSONArray("topMostRow").length();
	}
	public int getRowNum() 
	{
		return obj_.getJSONArray("leftMostColumn").length();
	}
	@Override
	public String processReply(String reply) 
	{
		return this.verify(reply,
				obj_.getJSONArray("data"),
				obj_.getJSONArray("topMostRow"),
				obj_.getJSONArray("leftMostColumn"));
	}
	@Override
	public String showTest() {
		JSONArray col = obj_.getJSONArray("leftMostColumn"),
				row = obj_.getJSONArray("topMostRow"),
				answer = obj_.getJSONArray("data");
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
		col = new JSONArray(col.toString());
		for(int i = col.length()-1; i>=0;i--)
			col.put(i+1,col.getString(i));
		col.put(0,CORNERSTONE);
		int colNum = row.length(),
				rowNum = col.length() - 1;
		logger_.info(String.format("colnum=%d, rownum=%d", colNum,rowNum));
		boolean isCorrect = true;
		TableBuilder tb = new TableBuilder();
		String[] tokens = reply.split(" ");
		if(tokens.length!=answer.length()) {
			return String.format("answer.length=(%d)!=(%d)", tokens.length,answer.length());
		}
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
	public static void AddTests(final ArrayList<JsonTest> testContainer, MongoClient mongoClient) throws Exception
	{
		MongoCollection<Document> tests = mongoClient.getDatabase("logistics").getCollection("paradigmTests");
		tests.find().forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				JSONObject obj = new JSONObject(arg0.toJson());
				testContainer.add(new ParadigmTest(obj));
			}
		});
	}
	@Override
	public String toString() {
		return String.format("%s(%dx%d)", obj_.getString("name"),getRowNum(),getColNum());
	}
	@Override
	public String[] isCalled() {
		return new String[] { String.format("paradigm test: %s\n%s", 
				obj_.getString("name"),toTable().toString())};
	}
	private TableBuilder toTable() {
//		private String verify(String reply,JSONArray answer,JSONArray row, JSONArray col)
//		return this.verify(reply,
//				obj_.getJSONArray("data"),
//				obj_.getJSONArray("topMostRow"),
//				obj_.getJSONArray("leftMostColumn"));
		JSONArray col = new JSONArray(obj_.getJSONArray("leftMostColumn").toString());
		for(int i = col.length()-1; i>=0;i--)
			col.put(i+1,col.getString(i));
		col.put(0,CORNERSTONE);
		JSONArray row = obj_.getJSONArray("topMostRow");
		int colNum = row.length(),
				rowNum = col.length() - 1;
		logger_.info(String.format("colnum=%d, rownum=%d", colNum,rowNum));
//		boolean isCorrect = true;
		TableBuilder tb = new TableBuilder();
//		String[] tokens = reply.split(" ");
		for(int i = 0; i < rowNum ; i++)
		{
			tb.newRow();
			for(int j = 0; j < colNum; j++)
			{
//				String answerS = answer.getString(rowNum*j+i),
//						correct = tokens[rowNum*j+i];
//				boolean isThisCorrect = answerS.equalsIgnoreCase(correct);
//				if(isThisCorrect)
//					numOfCorrectAnswers++;
//				isCorrect = isCorrect && isThisCorrect;
				tb.addToken("*");
			}
		}
//		logger_.info(String.format("tb=\n%s", tb.toString()));
		
		tb
			.addRow(row, 0)
			.addCol(col, 0);
		return tb;
	}
}
