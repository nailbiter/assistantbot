package managers.tests;

import org.json.JSONObject;

abstract public class Test
{
	JSONObject obj_ = null;
	int index_;
	public Test(JSONObject obj,int index)
	{
		obj_ = obj;
		index_ = index;
		schedule();
	}
	public void schedule()
	{
		//TODO
	}
}