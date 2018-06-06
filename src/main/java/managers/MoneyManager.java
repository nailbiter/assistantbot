package managers;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import assistantbot.MyAssistantUserData;
import util.MyBasicBot;
import util.parsers.StandardParser;

public class MoneyManager implements managers.MyManager,OptionReplier{
	JSONArray money = null;
	JSONObject categories = null;
	JSONArray cats = null;
	MyAssistantUserData ud_ = null;
	public MoneyManager(MyAssistantUserData myAssistantUserData)
	{
		JSONObject obj= util.StorageManager.get("money", true);
		if(!obj.has("array"))
			obj.put("array", new JSONArray());
		money = obj.getJSONArray("array");
		if(!obj.has("categories"))
			obj.put("categories", MoneyManager.makeInitCategories());
		categories = obj.getJSONObject("categories");
		
		cats = new JSONArray();
		for(int i = 0; i < categories.names().length(); i++)
			cats.put(categories.getString(categories.names().getString(i)));
		
		ud_ = myAssistantUserData;
	}
	protected static JSONObject makeInitCategories()
	{
		JSONObject cats = new JSONObject()
				.put("2", "fun")
				.put("3"	, "transport")
				.put("1", "food");
		return cats;
	}
	Hashtable<Integer,Integer> pendingOperations = new Hashtable<Integer,Integer>();
	public String putMoney(int amount)
	{
		int msgid = ud_.sendMessageWithKeyBoard("which category?", cats);
		this.pendingOperations.put(msgid, amount);
		return String.format("prepare to put %d",amount);
	}
	private void putMoney(int amount,String categoryName)
	{
		money.put(new JSONObject()
				.put("amount", amount)
				.put("category", categoryName)
				.put("date",new Date().toString()));
	}
	public String getLastCosts(int howMuch)
	{
		util.TableBuilder tb = new util.TableBuilder();
		tb.newRow();
		tb.addToken("amount");
		tb.addToken("category");
		tb.addToken("date");
		Hashtable<String,Integer> totals = new Hashtable<String,Integer>();
		for(int idx = money.length()-1;idx>=0&&howMuch>0;)
		{
			String category = money.getJSONObject(idx).getString("category");
			int amount = money.getJSONObject(idx).getInt("amount");
			tb.newRow();
			tb.addToken(amount);
			if(!totals.containsKey(category))
				totals.put(category, 0);
			totals.put(category, totals.get(category) + amount);
			tb.addToken(category);
			tb.addToken(money.getJSONObject(idx).getString("date"));
			howMuch--;
			idx--;
		}
		
		util.TableBuilder tb1 = new util.TableBuilder();
		tb1.newRow();
		tb1.addToken("category");
		tb1.addToken("total");
		Set<String> keys = totals.keySet();
		Iterator<String> itr = keys.iterator();
	    while (itr.hasNext()) { 
	       String key = itr.next();
	       tb1.newRow();
	       tb1.addToken(key);
	       tb1.addToken(totals.get(key));
	    }
		
		return tb.toString()+"\n------------------\n"+tb1.toString();
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
	public String getCategory(String alias) { return categories.getString(alias); }
	@Override
	public String getResultAndFormat(JSONObject res) throws Exception {
		if(res.has("name"))
		{
			System.out.println(this.getClass().getName()+" got comd: /"+res.getString("name"));
			if(res.getString("name").compareTo("moneycats")==0) 
				return getMoneyCats();
			if(res.getString("name").compareTo("money")==0) {
				return putMoney(res.getInt("amount")/*, res.getString("category")*/);
			}
			if(res.getString("name").equals("costs"))
				return getLastCosts(res.getInt("num"));
		}
		return null;
	}
	@Override
	public JSONArray getCommands() {
		JSONArray res = new JSONArray(
				"["+ 
				"{\"name\":\"costs\",\"args\":[{\"name\":\"num\",\"type\":\"int\",\"isOpt\":true}]}]");
		res.put(AbstractManager.makeCommand("money", "spent money", 
				Arrays.asList(AbstractManager.makeCommandArg(
						"amount",
						StandardParser.ArgTypes.integer, 
						false))));
		return res;
		//",{\"name\":\"money\",\"args\":[{\"name\":\"amount\",\"type\":\"int\"},{\"name\":\"category\",\"type\":\"string\",\"isOpt\":true}],\"help\":\"spent money\"}\n" +
	}
	@Override
	public String processReply(int messageID,String msg) {
		return null;
	}
	@Override
	public String optionReply(String option, Integer msgID) {
		if(this.pendingOperations.containsKey(msgID))
		{
			this.putMoney(this.pendingOperations.get(msgID), option);
			String res = String.format("put %d in category %s",
					this.pendingOperations.get(msgID),option);
			this.pendingOperations.remove(msgID);
			return res;
		}
		else
			return null;
	}
}