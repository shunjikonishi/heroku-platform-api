package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Region extends AbstractModel {
	
	public static final Region US = new Region("us");
	public static final Region EU = new Region("eu");
	
	public static Region valueOf(String name) {
		if ("us".equals(name)) return US;
		if ("eu".equals(name)) return EU;
		
		return null;
	}
	
	public Region() {
		super();
	}
	
	public Region(Map<String, Object> map) {
		super(map);
	}
	
	private Region(String name) {
		set("name", name);
	}
	
	public String getId() { return getAsString("id");}
	public String getName() { return getAsString("name");}
	
	public Date getCreatedAt() { return getAsDate("created_at");}
	public Date getUpdatedAt() { return getAsDate("updated_at");}
	
	public String getDescription() { return getAsString("description");}
	
	public String toString() { return getName();}
	
}
