package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Formation extends AbstractModel {
	
	public Formation() {
		super();
	}
	
	public Formation(Map<String, Object> map) {
		super(map);
	}
	
	public String getId() { return getAsString("id");}
	
	public Date getCreatedAt() { return getAsDate("created_at");}
	public Date getUpdatedAt() { return getAsDate("updated_at");}
	
	public String getCommand() { return getAsString("command");}
	public String getType() { return getAsString("type");}
	
	public int getQuantity() { return getAsInt("quantity");}
	public int getSize() { return getAsInt("size");}
	
}
