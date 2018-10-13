package managers.tests;

import org.json.JSONObject;

abstract public class Test {
	protected JSONObject obj_;
	abstract public String processReply(String msg);

	abstract public String showTest();

	public String getName() {
		return obj_.getString("name");
	}

	public String[] isCalled() {
		return new String[] {String.format("paradigm test: %s", 
				getName())};
	}
}
