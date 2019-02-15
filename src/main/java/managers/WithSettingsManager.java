package managers;

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assistantbot.ResourceProvider;
import util.Util;

public class WithSettingsManager extends AbstractManager {
	private static final String CHOOSETHESETTING = "choose the setting:";
	private static final String TYPE = "TYPE";
	private static enum MessageType {
		BUTTONS;
	}
	private static final String VAL = "VAL";
	protected ResourceProvider rp_;
	private Hashtable<String, JSONObject> settings_;
	protected WithSettingsManager(JSONArray commands, ResourceProvider rp) {
		super(commands);
		settings_ = new Hashtable<String,JSONObject>();
		rp_ = rp;
	}
	@Override
	public void set() {
		Map<String, Object> map = new Hashtable<String,Object>();
		for(final String key:settings_.keySet()) {
			JSONObject setting = settings_.get(key);
			if(setting.get(TYPE)==SettingsType.ENUM) {
				ImmutablePair<Object[],Integer> val = 
						(ImmutablePair<Object[], Integer>) setting.get(VAL);
				int index;
				try {
					index = ArrayUtils.indexOf(val.left, getSetting(key));
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				map.put(String.format("change \"%s\"", key), new JSONObject()
						.put(TYPE, MessageType.BUTTONS)
						.put(VAL
								, new ImmutableTriple<String,Map<String,Object>,Transformer<Object,String>>(
								String.format("pick your \"%s\"", key)
								, Util.IdentityMapWithSuffix(new JSONArray(val.left),index,"_")
								, new Transformer<Object,String>() {
									@Override
									public String transform(Object arg0) {
										setSetting(key,arg0);
										return String.format("set %s being equal to \"%s\"", key,arg0);
									}
								}
						)));
			} else {
				rp_.sendMessage(String.format("e: unknown type for setting \"%s\"", setting.toString(2)));
				return;
			}
		}
		
		Transformer<Object, String> me = new Transformer<Object,String>(){
			@Override
			public String transform(Object arg0) {
				JSONObject obj = (JSONObject) arg0;
				MessageType type = (MessageType) obj.get(TYPE);
				if( type == MessageType.BUTTONS ) {
					ImmutableTriple<String,Map<String,Object>,Transformer<Object,String>> val = 
							(ImmutableTriple<String, Map<String, Object>, Transformer<Object, String>>) obj.get(VAL);
					rp_.sendMessageWithKeyBoard(val.left, val.middle, val.right);
					return val.left;
				} else {
					return String.format("unknown type");
				}
			}
		};

		rp_.sendMessageWithKeyBoard(CHOOSETHESETTING,map,me);
	}
	protected void setSetting(String key, Object val) {
		rp_.setManagerSettingsObject(getName(), key, val);
	}
	protected Object getSetting(String name) throws JSONException, Exception {
		JSONObject po = getParamObject(rp_);
		Object res = po.opt(name);
		if( res == null )
			res = getDefault(name);
		return res;
	}
	protected static enum SettingsType{
		ENUM;
	}
	public void addSettingEnum(String name, Object[] values, int defaultIndex) {
		settings_.put(name, new JSONObject()
				.put(TYPE, SettingsType.ENUM)
				.put(VAL, new ImmutablePair<Object[],Integer>(values,defaultIndex))
				);
	}
	private Object getDefault(String name) {
		JSONObject setting = settings_.get(name);
		SettingsType type = (SettingsType) setting.get(TYPE);
		if( type == SettingsType.ENUM ) {
			ImmutablePair<Object[],Integer> val = 
					(ImmutablePair<Object[], Integer>) setting.get(VAL);
			return val.left[val.right];
		} else
			return null;
	}
}
