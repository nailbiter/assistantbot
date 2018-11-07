package managers.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.json.JSONObject;

public class RandomSetGenerator {
	Random rand_ = new Random();
	public static ArrayList<String> MakeRandomSet(ArrayList<JSONObject> data, int size) {
		return (new RandomSetGenerator()).makeRandomSet(data, size);
	}
	private ArrayList<String> makeRandomSet(ArrayList<JSONObject> data, int size) {
		ArrayList<String> res = new ArrayList<String>();
		ArrayList<JSONObject> distribution = new ArrayList<JSONObject>();
		while(res.size() < size) {
			System.out.format("iteration #%d\n", res.size()+1);
			GenerateDistribution(data,distribution);
			System.out.format("dist: %s\n", distribution.toString());
			String resS = pickElement(data,distribution);
			System.out.format("resS: %s\n", resS);
			res.add(resS);
			RemoveElementByKey(data,resS);
		}
		
		Collections.sort(res);
		return res;
	}
	private static void RemoveElementByKey(ArrayList<JSONObject> data, String resS) {
		for(int i = 0; i < data.size(); i++) {
			if(data.get(i).getString("key").equals(resS))
			{
				data.remove(i);
				return;
			}
		}
	}
	private String pickElement(ArrayList<JSONObject> data, ArrayList<JSONObject> distribution) {
		double pick = rand_.nextDouble()*distribution.get(distribution.size()-1).getDouble("upper");
		System.out.format("pick=%g\n", pick);
		for(int i = 0; i < distribution.size(); i++) {
			JSONObject obj = distribution.get(i);
			if(obj.getDouble("lower")<=pick && pick < obj.getDouble("upper"))
				return obj.getString("key");
		}
		return distribution.get(distribution.size()-1).getString("key");
	}
	private static void GenerateDistribution(ArrayList<JSONObject> data, ArrayList<JSONObject> distribution) {
		distribution.clear();
		double total = 0.0;
		for(int i = 0; i < data.size(); i++) {
			JSONObject obj = new JSONObject();
			obj.put("lower", total);
			total += data.get(i).getDouble("probability");
			obj.put("upper", total);
			obj.put("key", data.get(i).getString("key"));
			distribution.add(obj);
		}
	}
}
