package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class Addon extends BasicModel {
	
	public Addon() {
		super();
	}
	
	public Addon(Map<String, Object> map) {
		super(map);
	}
	
	public Addon(String planNameOrId) {
		if (planNameOrId.indexOf(":") != -1) {
			setPlanName(planNameOrId);
		} else {
			setPlanId(planNameOrId);
		}
	}
	
	public Map<String, Object> getConfig() { return (Map<String, Object>)get("config");}
	
	public String getPlanId() { return getAsString("plan.id");}
	public void setPlanId(String s) { set("plan.id", s);}
	
	public String getPlanName() { return getAsString("plan.name");}
	public void setPlanName(String s) { set("plan.name", s);}
	
	public void setConfig(String name, String value) {
		Map<String, Object> map = getConfig();
		if (map == null) {
			map = new HashMap<String, Object>();
			set("config", map);
		}
		map.put(name, value);
	}
}

