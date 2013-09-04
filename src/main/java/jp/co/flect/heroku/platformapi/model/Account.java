package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Account extends AbstractModel {
	
	public Account() {
		super();
	}
	
	public Account(Map<String, Object> map) {
		super(map);
	}
	
	public boolean allowTracking() { return getAsBoolean("allow_tracking");}
	public boolean isBeta() { return getAsBoolean("beta");}
	
	public Date getCreatedAt() { return getAsDate("created_at");}
	public Date getLastLogin() { return getAsDate("last_login");}
	public Date getUpdatedAt() { return getAsDate("updated_at");}
	
	public String getEmail() { return getAsString("email");}
	public String getId() { return getAsString("id");}
	
	public boolean isVerified() { return getAsBoolean("verified");}
	
}
