package managers.tests;

import org.json.JSONObject;

import managers.TestManager;
import util.MyManager;

public class ParadigmTest extends Test {
	public ParadigmTest(JSONObject obj,TestManager master,String name) {
		super(obj,master,name);
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
		if(count == 0)
			return this.verifySixteen(reply, new String[] {
					"der","den","des","dem",
					"die","die","der","der",
					"das","das","des","dem",
					"die","die","der","den"});
		if(count == 1)
			return this.verifySixteen(reply, new String[] {
					"welcher","welchen","welches","welchem",
					"welche","welche","welcher","welcher",
					"welches","welches","welches","welchem",
					"welche","welche","welcher","welchen"});
		if(count == 2)
			return this.verifyTwelve(reply, new String[] {
					"ein","einen","eines","einem",
					"eine","eine","einer","einer",
					"ein","ein","eines","einem"});
		if(count == 3)
			return this.verifySixteen(reply, new String[] {
					"mein","meinen","meines","meinem",
					"meine","meine","meiner","meiner",
					"mein","mein","meines","meinem",
					"meine","meine","meiner","meinen"});
		return null;
	}
	private String verifyTwelve(String reply,String[] answer)
	{
		boolean isCorrect = true;
		util.TableBuilder tb = new util.TableBuilder()
				.addNewlineAndTokens(new String[] {"--","M","F","N"});
		String[] tokens = reply.split(" ");
		for(int i = 0; i < 4 ; i++)
		{
			tb.newRow();
			switch(i)
			{
			case 0:
				tb.addToken("N");
			case 1:
				tb.addToken("A");
			case 2:
				tb.addToken("G");
			case 3:
				tb.addToken("D");
			}
			
			for(int j = 0; j < 3; j++)
			{
				String answerS = answer[4*j+i],
						correct = tokens[4*j+i];
				boolean isThisCorrect = answerS.equals(correct);
				isCorrect = isCorrect && isThisCorrect;
				tb.addToken(isThisCorrect ? answerS : String.format("<%s>", answerS));
			}
		}
		return tb.toString();
	}
	private String verifySixteen(String reply,String[] answer)
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
	}

}
