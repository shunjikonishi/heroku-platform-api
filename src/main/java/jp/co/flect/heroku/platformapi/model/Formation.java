package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Formation extends BasicModel {
	
	public Formation() {
		super();
	}
	
	public Formation(Map<String, Object> map) {
		super(map);
	}
	
	public String getCommand() { return getAsString("command");}
	public String getType() { return getAsString("type");}
	
	public int getQuantity() { return getAsInt("quantity");}
	public int getSize() { return getAsInt("size");}
	
}
