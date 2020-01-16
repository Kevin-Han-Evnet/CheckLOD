package com.example.testforble.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Json 파싱 관련 유틸리티
 * @author jungeh
 *
 */
public class JsonUtils {

	public static HashMap<String, Object> parse(JSONObject json) {
		return parseObject(json);
	}

	private static HashMap<String, Object> parseObject(JSONObject jo) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		Iterator<?> it = jo.keys();
		while (it.hasNext()) {
			String key = (String) it.next();
			try {
				Object o = jo.get(key);
				if (o instanceof String) {
					map.put(key, o);
				} else if (o instanceof Boolean || o instanceof Integer || o instanceof Float || o instanceof Double) {
					map.put(key, String.valueOf(o));
				} else if (o instanceof JSONArray) {
					ArrayList<?> array = parseArray((JSONArray) o);
					map.put(key, array);
				} else if (o instanceof JSONObject) {
					map.put(key, parseObject((JSONObject) o));
				} else {
					map.put(key, String.valueOf(o));
				}
			} catch (JSONException e) {
				/** 이제그만~*///LogUtil.E(e.toString());
			}
		}

		return map;
	}

	private static ArrayList<?> parseArray(JSONArray ja) {
		ArrayList<Map<String, Object>> array;
		int len = ja.length();

		array = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < len; i++) {
			JSONObject jo;
			try {
				jo = ja.getJSONObject(i);
				array.add(parseObject(jo));
			} catch (JSONException e) {
				/** 이제그만~*///LogUtil.E(e.toString());
			}
		}
		return array;
	}

	public static void autoMappingJsonToObject(JSONObject json, Object obj) {
		Field[] fields = obj.getClass().getFields();
		for (Field field : fields) {

			/*
			LogUtil.I ("[뭐어쩌라는겨] -- " + field.getName() + " | " + field.getType());
			LogUtil.I ("[뭐어쩌라는겨] --> " + field.getName() + " | " + field.getType().equals(boolean.class));
			LogUtil.I ("[뭐어쩌라는겨] --x " + field.getName() + " | " + field.getType().isInstance (boolean.class));
			*/

			if (field.getType().equals(String.class)) {
				try {
					field.set(obj, json.optString(field.getName()));
				} catch (Exception e) {
					//nohting yet;
				}
			} else if (field.getType().equals(int.class)) {
				try {
					field.set(obj, json.optInt(field.getName()));
				} catch (Exception e) {
					//nohting yet;
				}
			} else if (field.getType().equals(double.class)) {
				try {
					field.set(obj, json.optDouble(field.getName()));
				} catch (Exception e) {
					//nohting yet;
				}
			} else if (field.getType().equals(boolean.class)) {
				try {
					field.set(obj, json.optBoolean(field.getName()));
				} catch (Exception e) {
					try {
						field.set(obj, StringUtils.convertStrimgToBoolean (json.optString (field.getName())));
					} catch (Exception e1) {
						//nothing yet;
					}
				}
			} else {
				try {
					field.set(obj, json.optString(field.getName()));
				} catch (Exception e) {
					//nohting yet;
				}
			}

		}
	}
	
	public static String getStringNoException(JSONObject json, String key)
	{
	    String output = new String("");
	    try{
	        output = json.getString(key);
	    }catch(Exception e){
	        output = new String("");
	    }
	    return output;
	}
}
