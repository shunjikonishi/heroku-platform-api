package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Region extends BasicModel {
	
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
	
	public String getName() { return getAsString("name");}
	
	public String getDescription() { return getAsString("description");}
	
	public String toString() { return getName();}
	
}
