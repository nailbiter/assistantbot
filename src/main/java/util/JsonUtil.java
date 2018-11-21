package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtil {

	public static JSONObject FindInJSONArray(JSONArray array,String key,String val) {
		for(Object o: array) {
			JSONObject obj = (JSONObject)o;
			if(obj.getString(key).equals(val))
				return obj;
		}
		return null;
	}

	public static void SortJSONArray(JSONArray arr,String key)
	{
		List<JSONObject> list = new ArrayList<JSONObject>();
		final String keyFinal = key;
		for(int idx = 0; idx < arr.length(); )
		{
			JSONObject obj = arr.optJSONObject(idx);
			if(obj==null)
				idx++;
			else
			{
				arr.remove(idx);
				list.add(obj);
			}
		}
		Collections.sort(list,new Comparator<JSONObject>()
				{
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						return o1.getString(keyFinal).compareTo(o2.getString(keyFinal));
					}
				});
		for(int i = 0; i < list.size(); i++)
			arr.put(list.get(i));
	}
	public static void CopyIntoJson(JSONObject dest,JSONObject src) {
		for(String key:src.keySet())
			dest.put(key, src.get(key));
	}
	public static void CapitalizeJsonKeys(JSONObject obj) {
		Set<String> keys = new HashSet<String>(obj.keySet());
		for(String key:keys) {
			Object val = obj.get(key);
			obj.remove(key);
			obj.put(key.toUpperCase(), val);
		}
	}
	public static void FilterJsonKeys(JSONObject obj, JSONArray keys) {
		List<Object> listOfKeys = keys.toList();
		List<String> objKeys = new ArrayList<String>(obj.keySet());
		for(String key: objKeys) {
			if( !listOfKeys.contains(key) )
				obj.remove(key);
		}
	}
}
