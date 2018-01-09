package managers.tests;

import org.json.JSONArray;
import org.json.JSONObject;

import managers.TestManager;
import util.LocalUtil;
import util.MyManager;

public class PluralTest extends Test {
	JSONArray germanplurals = null;
	public PluralTest(JSONObject obj,TestManager master,String name) throws Exception {
		super(obj,master,name);
		germanplurals = LocalUtil.getJSONArrayFromRes(this, "germanplurals");
		System.out.println(String.format("germanplurals has %d elements", germanplurals.length()));
	}

	@Override
	protected String isCalled(int count) {
		// TODO Auto-generated method stub
		return String.format("isCalled with %d", count);
	}

	@Override
	public String processReply(String reply,int count) {
		// TODO Auto-generated method stub
		return String.format("processReply with reply=%s, count=%d", reply,count);
	}
}
