package jp.co.flect.heroku.json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonUtils {
	
	private static final Json json;
	
	static {
		Json ret = null;
		try {
			Class.forName("com.google.gson.Gson");
			ret = new JsonByGson();
		} catch (Exception e) {
		}
		if (ret == null) {
			try {
				Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
				ret = new JsonByJackson();
			} catch (Exception e) {
			}
		}
		if (ret == null) {
			throw new IllegalStateException("Jsonlibraries not found.");
		}
		json = ret;
	}
	
	public static List<Map<String, Object>> parseArray(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		char c = str.charAt(0);
		if (c == '[') {
			return json.parseArray(str);
		} else if (c == '{') {
			Map<String, Object> map = json.parse(str);
			return Arrays.asList(map);
		} else {
			throw new IllegalArgumentException(str);
		}
	}
	
	public static Map<String, Object> parse(String str) {
		return json.parse(str);
	}
	
	public static <T> T parse(String str, Class<T> clazz) {
		return json.parse(str, clazz);
	}
	
	public static String serialize(Object obj) {
		return json.serialize(obj);
	}
}
