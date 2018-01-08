package managers.tests;

import java.util.Timer;

import org.json.JSONObject;

abstract public class Test
{
	protected JSONObject obj_ = null;
	Timer timer_ = null;
	public Test(JSONObject obj)
	{
		obj_ = obj;
		schedule();
	}
	protected void schedule()
	{
		//TODO
		timer_ = new Timer();
	}
	abstract protected String isCalled(int count);
	abstract protected String processReply(String reply);
}