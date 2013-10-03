package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public abstract class BasicModel extends AbstractModel {
	
	public BasicModel() {
		super();
	}
	
	public BasicModel(Map<String, Object> map) {
		super(map);
	}
	
	public final String getId() { return getAsString("id");}
	
	public final Date getCreatedAt() { return getAsDate("created_at");}
	public final Date getUpdatedAt() { return getAsDate("updated_at");}
	
}
