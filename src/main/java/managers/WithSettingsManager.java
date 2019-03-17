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
import util.Message;
import util.Util;
import util.parsers.ParseOrdered;
import util.parsers.ParseOrdered.ArgTypes;

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
	public void set() throws Exception {
		Map<String, Object> map = new Hashtable<String,Object>();
		for(final String key:settings_.keySet()) {
			JSONObject setting = settings_.get(key);
			switch((SettingsType)setting.get(TYPE)) {
			case ENUM:
				ImmutableTriple<String[], Object[], Integer> val = 
					(ImmutableTriple<String[],Object[],Integer>) setting.get(VAL);
				int index;
				try {
					index = ArrayUtils.indexOf(val.middle, getSetting(key));
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				Map<String, Object> mmap = new Hashtable<String, Object>();
				for(int i = 0; i < val.left.length;i++) {
					mmap.put(val.left[i]+((i==index)?"_":""), val.middle[i]);
				}
				
				map.put(String.format("change \"%s\"", key), new JSONObject()
						.put(TYPE, MessageType.BUTTONS)
						.put(VAL
								, new ImmutableTriple<String,Map<String,Object>,Transformer<Object,Message>>(
								String.format("pick your \"%s\"", key)
								, mmap
								, new Transformer<Object,Message>() {
									@Override
									public Message transform(Object arg0) {
										setSetting(key,arg0);
										return new Message(String.format("set %s being equal to \"%s\"", key,arg0));
									}
								}
						)));
				break;
			case SCALAR:
				ImmutablePair<ParseOrdered.ArgTypes,Object> sval = (ImmutablePair<ArgTypes, Object>) setting.get(VAL);
				rp_.sendMessage(new Message(String.format("reply to this message to set %s of type %s", key, sval.left))
						, new Transformer<String,Message>() {
							@Override
							public Message transform(String input) {
								try {
									Object parsedVal = ParseOrdered.ParseType(sval.left.toString(), input);
									setSetting(key,parsedVal);
									return new Message(String.format("set %s being equal to \"%s\"", key, parsedVal));
								} catch (Exception e) {
									e.printStackTrace();
									return new Message(Util.ExceptionToString(e));
								}
							}
				});
				break;
			default:
				rp_.sendMessage(new Message(String.format("e: unknown type for setting \"%s\"", setting.toString(2))));
				return;
			}
		}
		
		Transformer<Object, Message> me = new Transformer<Object, Message>(){
			@Override
			public Message transform(Object arg0) {
				JSONObject obj = (JSONObject) arg0;
				MessageType type = (MessageType) obj.get(TYPE);
				switch(type) {
				case BUTTONS:
					ImmutableTriple<String,Map<String,Object>,Transformer<Object,Message>> val = 
						(ImmutableTriple<String, Map<String, Object>, Transformer<Object, Message>>) obj.get(VAL);
					rp_.sendMessageWithKeyBoard(new Message(val.left), val.middle, val.right);
					return new Message(val.left);
//					break;
				default:
//					break
					return new Message(String.format("unknown type"));
				}
			}
		};

		rp_.sendMessageWithKeyBoard(new Message(CHOOSETHESETTING),map,me);
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
		ENUM, SCALAR;
	}
	public void addSettingEnum(String name, String[] names,Object[] values, int defaultIndex) {
		settings_.put(name, new JSONObject()
				.put(TYPE, SettingsType.ENUM)
				.put(VAL, new ImmutableTriple<String[],Object[],Integer>(names,values,defaultIndex))
				);
	}
	public void addSettingScalar(String name, ParseOrdered.ArgTypes type, Object defaultValue) {
		settings_.put(name, new JSONObject()
				.put(TYPE, SettingsType.SCALAR)
				.put(VAL, new ImmutablePair<ParseOrdered.ArgTypes,Object>(type,defaultValue))
				);
	}
	private Object getDefault(String name) {
		JSONObject setting = settings_.get(name);
		SettingsType type = (SettingsType) setting.get(TYPE);
		switch(type) {
		case ENUM:
		{
			ImmutableTriple<String[],Object[],Integer> val = 
					(ImmutableTriple<String[], Object[], Integer>) setting.get(VAL);
				return val.middle[val.right];
		}
		case SCALAR:
		{
			ImmutablePair<ParseOrdered.ArgTypes,Object> val = (ImmutablePair<ArgTypes, Object>) setting.get(VAL);
			return val.right;
		}
		default:
			return null;
		}
	}
}
