package util;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

public class TableBuilder {
	ArrayList<ArrayList<String>> tokens = new ArrayList<ArrayList<String>>();
	ArrayList<Integer> lengths = new ArrayList<Integer>();
	public void newRow()
	{
		tokens.add(new ArrayList<String>());
	}
	public void addToken(String token)
	{
		tokens.get(tokens.size()-1).add(token);
		int idx = tokens.get(tokens.size()-1).size() - 1;
		if(lengths.size()<=idx)
			lengths.add(0);
		if(lengths.get(idx).compareTo(token.length())<0)
			lengths.set(idx, token.length());
	}
	public void addToken(int value)
	{
		addToken(Integer.toString(value));
	}
	@Override
	public String toString()
	{
		for(int i = 0;i < lengths.size(); i++)
			System.out.print(lengths.get(i)+" ");
		System.out.println("");
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < tokens.size(); i++)
		{
			for(int j = 0; j < tokens.get(i).size(); j++)
			{
				sb.append(StringUtils.rightPad(tokens.get(i).get(j), lengths.get(j)+3));
			}
			sb.append("\n");
		}
		if(true)
			return sb.toString();
		else
			return "oentiheutni";
	}
}
