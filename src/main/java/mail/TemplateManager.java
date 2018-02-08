package mail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.StorageManager;

class TemplateManager {
	protected JSONObject metainfo_ = null;
	TemplateManager()
	{
		metainfo_ = StorageManager.get("templates", false);
	}
	MailTemplate getMailTemplate(String name) throws Exception
	{
		JSONArray templates = metainfo_.getJSONArray("templates");
		for(int i = 0; i < templates.length(); i++)
		{
			JSONObject t = templates.getJSONObject(i);
			if(t.getString("name").equals(name))
				return new MailTemplate(t.getString("handle"));
		}
		return null;
	}
	JSONArray getTemplateNames() {
		// TODO Auto-generated method stub
		JSONArray res = new JSONArray(),
				templates = metainfo_.getJSONArray("templates");
		for(int i = 0; i < templates.length(); i++)
			res.put(templates.getJSONObject(i).getString("name"));
		return res;
	}
}
