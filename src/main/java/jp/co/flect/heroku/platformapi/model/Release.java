package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Release extends AbstractModel {
	
	public Release() {
		super();
	}
	
	public Release(Map<String, Object> map) {
		super(map);
	}
	
	public String getId() { return getAsString("id");}
	
	public Date getCreatedAt() { return getAsDate("created_at");}
	public Date getUpdatedAt() { return getAsDate("updated_at");}
	
	public String getUserEmail() { return getAsString("user.email");}
	public String getUserId() { return getAsString("user.id");}
	public int getVersion() { return getAsInt("version");}
	
	public String getDescription() { return getAsString("description");}
	
	public String getSlugId() { return getAsString("sulg.id");}
}
