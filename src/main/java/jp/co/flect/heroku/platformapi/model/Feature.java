package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Feature extends AbstractModel {
	
	public Feature() {
		super();
	}
	
	public Feature(Map<String, Object> map) {
		super(map);
	}
	
	public String getId() { return getAsString("id");}
	public String getName() { return getAsString("name");}
	
	public Date getCreatedAt() { return getAsDate("created_at");}
	public Date getUpdatedAt() { return getAsDate("updated_at");}
	
	public String getDescription() { return getAsString("description");}
	public String getDocUrl() { return getAsString("doc_url");}
	
	public boolean isEnabled() { return getAsBoolean("enabled");}
	
}
