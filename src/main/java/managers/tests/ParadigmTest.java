package managers.tests;

import org.json.JSONArray;
import org.json.JSONObject;

import managers.TestManager;
import util.MyManager;

public class ParadigmTest extends Test {
	public ParadigmTest(JSONObject obj,JSONObject data,TestManager master,String name) throws Exception {
		super(obj,data,master,name);
	}
	@Override
	public String toString()
	{
		return String.format("start=%s, end=%s, count=%d", 
				obj_.getString("start"),
				obj_.getString("end"),
				obj_.getInt("count"));
	}
	@Override
	protected String isCalled(int count) {
		if(count == 0)
			return "test: the";
		if(count == 1)
			return "test: welch";
		if(count == 2)
			return "test: a";
		if(count == 3)
			return "test: mein";
		return null;
	}
	@Override
	public String processReply(String reply, int count) {
		System.out.println(String.format("paradigm: processReply(count=%d,obj_=%s)",count,obj_.toString()));
		return this.verify(reply,obj_.getJSONArray("data").getJSONArray(count));
	}
	private String verify(String reply,JSONArray answer)
	{
		int colNum = answer.length() / 4;
		boolean isCorrect = true;
		String[] genders = (colNum == 3) ? new String[] {"--","M","F","N"} : new String[] {"--","M","F","N","Pl"},
				cases = new String[] {"N", "A","G","D"};
		util.TableBuilder tb = new util.TableBuilder().addNewlineAndTokens(genders);
		String[] tokens = reply.split(" ");
		for(int i = 0; i < 4 ; i++)
		{
			tb.newRow().addToken(cases[i]);
			/*switch(i)
			{
			case 0:
				tb.addToken("N");
				break;
			case 1:
				tb.addToken("A");
				break;
			case 2:
				tb.addToken("G");
				break;
			case 3:
				tb.addToken("D");
				break;
			}*/
			
			for(int j = 0; j < colNum; j++)
			{
				String answerS = answer.getString(4*j+i),
						correct = tokens[4*j+i];
				boolean isThisCorrect = answerS.equalsIgnoreCase(correct);
				isCorrect = isCorrect && isThisCorrect;
				tb.addToken(isThisCorrect ? answerS : String.format("<%s>", answerS));
			}
		}
		return tb.toString();
	}
	/*private String verifySixteen(String reply,String[] answer)
	{
		boolean isCorrect = true;
		util.TableBuilder tb = new util.TableBuilder()
				.addNewlineAndTokens(new String[] {"--","M","F","N","Pl"});
		String[] tokens = reply.split(" ");
		for(int i = 0; i < 4 ; i++)
		{
			tb.newRow();
			switch(i)
			{
				case 0:
					tb.addToken("N");
					break;
				case 1:
					tb.addToken("A");
					break;
				case 2:
					tb.addToken("G");
					break;
				case 3:
					tb.addToken("D");
					break;
			}
			
			for(int j = 0; j < 4; j++ )
			{
				String answerS = answer[4*j+i],
						correct = tokens[4*j+i];
				boolean isThisCorrect = answerS.equals(correct);
				isCorrect = isCorrect && isThisCorrect;
				tb.addToken(isThisCorrect ? answerS : String.format("<%s>", answerS));
			}
		}
		return tb.toString();
	}*/
}
