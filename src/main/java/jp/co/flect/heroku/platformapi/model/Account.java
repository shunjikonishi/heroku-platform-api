package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Account extends BasicModel {
	
	public Account() {
		super();
	}
	
	public Account(Map<String, Object> map) {
		super(map);
	}
	
	public boolean allowTracking() { return getAsBoolean("allow_tracking");}
	public boolean isBeta() { return getAsBoolean("beta");}
	
	public Date getLastLogin() { return getAsDate("last_login");}
	
	public String getEmail() { return getAsString("email");}
	
	public boolean isVerified() { return getAsBoolean("verified");}
	
}
