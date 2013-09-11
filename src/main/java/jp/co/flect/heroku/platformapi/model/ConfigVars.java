package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.HashMap;

public class ConfigVars extends AbstractModel {
	
	public ConfigVars() {
		super();
	}
	
	public ConfigVars(Map<String, Object> map) {
		super(map);
	}
	
	@Override
	public String get(String key) { 
		return (String)super.get(key);
	}
	
	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<String, String>();
		for (String key : keys()) {
			map.put(key, get(key));
		}
		return map;
	}
	
}
