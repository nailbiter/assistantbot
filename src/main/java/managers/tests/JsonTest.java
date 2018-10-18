package managers.tests;

import org.json.JSONObject;

abstract public class JsonTest {
	protected JSONObject obj_;
	abstract public String processReply(String msg);

	abstract public String showTest();

	public String getName() {
		System.err.format("obj=%s, return %s", obj_.toString(),obj_.getString("name"));
		return obj_.getString("name");
	}

	abstract public String[] isCalled();
	@Override
	public String toString() {
		return getName();
	}
}
