package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class LogDrain extends BasicModel {
	
	public LogDrain() {
		super();
	}
	
	public LogDrain(Map<String, Object> map) {
		super(map);
	}
	
	public String getAddonId() { return getAsString("addon.id");}
	public String getUrl() { return getAsString("url");}
}

