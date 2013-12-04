package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class Stack extends BasicModel {
	
	public Stack() {
		super();
	}
	
	public Stack(Map<String, Object> map) {
		super(map);
	}
	
	private Stack(String name) {
		set("name", name);
	}
	
	public String getName() { return getAsString("name");}
	public String getState() { return getAsString("state");}
	
	public String toString() { return getName();}
	
}
