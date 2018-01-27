package managers.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import managers.TestManager;
import util.LocalUtil;
import util.MyManager;

public class PluralTest extends Test {
	JSONArray germanplurals = null;
	Random rand = new Random();
	List<Integer> indexes_ = new ArrayList<Integer>();
	public PluralTest(JSONObject obj,JSONObject data,TestManager master,String name) throws Exception {
		super(obj,data,master,name);
		germanplurals = LocalUtil.getJSONArrayFromRes(this, "germanplurals");
		System.out.println(String.format("germanplurals has %d elements", germanplurals.length()));
	}

	@Override
	protected String isCalled(int count) {
		while((indexes_.size()-1) < count)
			indexes_.add(rand.nextInt(germanplurals.length()));
		int index = indexes_.get(count);
		
		return String.format("what is english, gender and plural for %s", germanplurals
				.getJSONObject(index)
				.getString("word"));
	}

	@Override
	public String processReply(String reply,int count) {
		JSONObject obj = germanplurals.getJSONObject(indexes_.get(count));
		//return germanplurals.getJSONObject(indexes_.get(count)).toString();
		return String.format("english=%s, article=%s, plural=%s",
				obj.getString("english"),
				obj.getString("article"),
				obj.getString("plural"));
	}
}
