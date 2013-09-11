package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Collaborator extends AbstractModel {
	
	public Collaborator() {
		super();
	}
	
	public Collaborator(Map<String, Object> map) {
		super(map);
	}
	
	public String getId() { return getAsString("id");}
	
	public String getUserEmail() { return getAsString("user.email");}
	public String getUserId() { return getAsString("user.id");}
	
	public Date getCreatedAt() { return getAsDate("created_at");}
	public Date getUpdatedAt() { return getAsDate("updated_at");}
	
	public boolean isSilent() { return getAsBoolean("silent");}
	
}
