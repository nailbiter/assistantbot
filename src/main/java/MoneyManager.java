import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class MoneyManager {
	JSONArray money = null;
	JSONObject categories = null;
	public MoneyManager(MyAssistantBot bot)
	{
		JSONObject obj= util.StorageManager.get("money", true);
		if(!obj.has("array"))
			obj.put("array", new JSONArray());
		money = obj.getJSONArray("array");
		if(!obj.has("categories"))
			obj.put("categories", MoneyManager.makeInitCategories());
		categories = obj.getJSONObject("categories");
	}
	protected static JSONObject makeInitCategories()
	{
		JSONObject cats = new JSONObject()
				.put("2", "fun")
				.put("3"	, "transport")
				.put("1", "food");
		return cats;
	}
	public void putMoney(int amount,String categoryAlias)
	{
		//TODO
		money.put(new JSONObject()
				.put("amount", amount)
				.put("category", categories.get(categoryAlias))
				.put("date",new Date().toString()));
	}
	public String getLastCosts(int howMuch)
	{
		//TODO
		util.TableBuilder tb = new util.TableBuilder();
		tb.newRow();
		tb.addToken("amount");
		tb.addToken("category");
		tb.addToken("date");
		for(int idx = money.length()-1;idx>=0&&howMuch>=0;)
		{
			tb.newRow();
			tb.addToken(money.getJSONObject(idx).getInt("amount"));
			tb.addToken(money.getJSONObject(idx).getString("category"));
			tb.addToken(money.getJSONObject(idx).getString("date"));
			howMuch--;
			idx--;
		}
		return tb.toString();
	}
	public String getMoneyCats()
	{
		util.TableBuilder tb = new util.TableBuilder();
		tb.newRow();
		tb.addToken("name");tb.addToken("alias");
		String[] names = JSONObject.getNames(categories);
		for(int i = 0; i < names.length; i++)
		{
			tb.newRow();
			tb.addToken(categories.getString(names[i]));
			tb.addToken(names[i]);
		}
		return tb.toString();
	}
}