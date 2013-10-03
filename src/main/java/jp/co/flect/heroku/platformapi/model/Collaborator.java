package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Collaborator extends BasicModel {
	
	public Collaborator() {
		super();
	}
	
	public Collaborator(Map<String, Object> map) {
		super(map);
	}
	
	public String getUserEmail() { return getAsString("user.email");}
	public String getUserId() { return getAsString("user.id");}
	
	public boolean isSilent() { return getAsBoolean("silent");}
	
	public String getRole() { return getAsString("role");}
	
	public boolean isOwner() { return "owner".equalsIgnoreCase(getRole());}
}
