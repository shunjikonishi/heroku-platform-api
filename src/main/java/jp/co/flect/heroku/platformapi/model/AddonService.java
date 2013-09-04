package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class AddonService extends AbstractModel {
	
	public AddonService() {
		super();
	}
	
	public AddonService(Map<String, Object> map) {
		super(map);
	}
	
	public String getId() { return getAsString("id");}
	public String getName() { return getAsString("name");}
	public Date getCreatedAt() { return getAsDate("created_at");}
	public Date getUpdatedAt() { return getAsDate("updated_at");}
	
}

