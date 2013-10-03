package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Feature extends BasicModel {
	
	public Feature() {
		super();
	}
	
	public Feature(Map<String, Object> map) {
		super(map);
	}
	
	public String getName() { return getAsString("name");}
	
	public String getDescription() { return getAsString("description");}
	public String getDocUrl() { return getAsString("doc_url");}
	
	public boolean isEnabled() { return getAsBoolean("enabled");}
	
}
