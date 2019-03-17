package managers.money;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import assistantbot.ResourceProvider;
import managers.AbstractManager;
import util.AssistantBotException;
import util.UserCollection;
import util.Util;
import util.parsers.ArithmeticExpressionParser;
import util.parsers.FlagParser;

public class MoneyManagerBase extends AbstractManager {

	protected static final String CATEGORIES = "categories";
	protected static final String DECSIGNS = "decsigns";
	protected static final String AMOUNT = "amount";
	protected static final String COMMENT = "comment";
	protected static double StringToAm(String amountString, JSONObject paramObject, MongoCollection<Document> money) throws JSONException, AssistantBotException {
		final JSONObject dispatch = new JSONObject()
			.put("d", Calendar.DATE)
			.put("m", Calendar.MONTH);
		Matcher m;
		if((m = Pattern.compile(String.format("(\\d*)([%s])", Util.CharSetToRegex(dispatch.keySet())))
				.matcher(amountString)).matches()) {
			final int field = dispatch.getInt(m.group(2));
			final MutableInt now = new MutableInt(Calendar.getInstance().get(field));
			final MutableInt mi = new MutableInt(0)
					,lim = new MutableInt(m.group(1).isEmpty()?1:Integer.parseInt(m.group(1)));
			money.find().sort(Sorts.descending("date"))
			.forEach(new Block<Document>() {
				@Override
				public void apply(Document arg0) {
					if(lim.intValue()>0) {
						Calendar c = Calendar.getInstance();
						c.setTime(arg0.getDate("date"));
						System.err.format("(%d vs %d) for %f\n"
								, now.intValue()
								, c.get(field)
								, arg0.getDouble("amount")
								);
						if( now.intValue() == c.get(field) ) {
							mi.increment();
						} else {
							lim.decrement();
							if( lim.intValue()>0 )
								mi.increment();
							now.setValue(c.get(field));
						}
					}
				}
			});
			return -mi.doubleValue();
		} else {
			return new ArithmeticExpressionParser(paramObject.getInt(DECSIGNS))
					.simpleEvalDouble(amountString);
		}
	}
	protected static String ShowTags(MongoCollection<Document> money, ResourceProvider rp) {
		final StringBuilder sb = new StringBuilder("tags: \n");
		final HashSet<String> set = new HashSet<String> ();
		money
		.find()
		.forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				JSONArray arr;
				try {
					arr = new JSONObject(arg0.toJson()).getJSONArray("tags");
				} catch(JSONException e) {
					return;
				}
				for(Object o:arr)
					set.add((String)o);
			}
		});
		
		ArrayList<String> rres = new ArrayList<String>(set);
		Collections.sort(rres);
		for(String tag:rres)
			sb.append(String.format("%s%s\n", "  ",tag));
		return sb.toString();
	}
	private String oldRemoveCostsValue_;
	protected ResourceProvider rp_;
	protected HashSet<String> cats_ = new HashSet<String>();
	protected MongoCollection<Document> money_;
	protected FlagParser fp_;

	protected MoneyManagerBase(JSONArray commands,ResourceProvider rp) throws JSONException, Exception {
		super(commands);
		rp_ = rp;
		for(Object o:this.getParamObject(rp).getJSONArray(CATEGORIES)) {
			cats_.add((String) o);
		}
		money_ = rp.getCollection(UserCollection.MONEY);
		fp_ = new FlagParser()
				.addFlag('t', "show tags")
				.addFlag('c', "show comments");
	}

	protected String removeCategory(String catname) {
		cats_.remove(catname);
		rp_.setManagerSettingsObject(getName(), CATEGORIES, new JSONArray(cats_));
		return String.format("removed category \"%s\"", catname);
	}

	protected String addCategory(String catname) {
		cats_.add(catname);
		rp_.setManagerSettingsObject(getName(), CATEGORIES, new JSONArray(cats_));
		return String.format("added category \"%s\"", catname);
	}

	protected String checkOperation(String command, String body) {
		if( command.toLowerCase().equals("r") || command.toLowerCase().equals("у") ) {
			return (removeCosts(body));
		} else {
			return null;
		}
	}

	private String removeCosts(String optString) {
		if(optString.isEmpty())
			optString = oldRemoveCostsValue_;
		oldRemoveCostsValue_ = optString;
		
		int pos = -1;
		final String SEPARATOR = "-";
		if( (pos = optString.indexOf(SEPARATOR))>=0 ) {
			String split1 = optString.substring(0, pos),
					split2 = optString.substring(pos+SEPARATOR.length());
			return removeCosts(Integer.parseInt(split1),Integer.parseInt(split2)+1);
		} else {
			int x = Integer.parseInt(optString);
			return removeCosts(x,x+1);
		}
	}

	private String removeCosts(final int from, final int till) {
		final HashSet<ObjectId> ids = new HashSet<ObjectId>();
		final MutableInt i = new MutableInt(0);
		money_.find().sort(Sorts.descending("date"))
		.forEach(new Block<Document>() {
			@Override
			public void apply(Document arg0) {
				int ipp = i.addAndGet(1);
				if(from<=ipp && ipp<till) {
					ObjectId id = arg0.getObjectId("_id");
					ids.add(id);
					System.err.format("adding id \"%s\" to deletion\n", id);
				}
			}
		});
		
		StringBuilder sb = new StringBuilder();
		for(ObjectId id:ids) {
			sb.append(String.format("%s\n", id));
			money_.deleteOne(Filters.eq("_id", id));
		}
		return String.format("removed costs #%d..#%d", from,till-1);
	}

}
