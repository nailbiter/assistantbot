package managers.tests;

import org.json.JSONObject;

import managers.TestManager;
import util.MyManager;

public class PluralTest extends Test {

	public PluralTest(JSONObject obj,TestManager master,String name) {
		super(obj,master,name);
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
