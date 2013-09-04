package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Addon extends AbstractModel {
	
	public Addon() {
		super();
	}
	
	public Addon(Map<String, Object> map) {
		super(map);
	}
	
	public String getId() { return getAsString("id");}
	public Date getCreatedAt() { return getAsDate("created_at");}
	public Date getUpdatedAt() { return getAsDate("updated_at");}
	
	public Map<String, Object> getConfig() { return (Map<String, Object>)get("config");}
	
	public String getPlanId() { 
		Map<String, Object> plan = (Map<String, Object>)get("plan");
		return plan == null ? null : (String)plan.get("id");
	}
	
	public String getPlanName() { 
		Map<String, Object> plan = (Map<String, Object>)get("plan");
		return plan == null ? null : (String)plan.get("name");
	}
	
}

