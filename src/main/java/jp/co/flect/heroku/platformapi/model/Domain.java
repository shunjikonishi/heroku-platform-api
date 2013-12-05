package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class Domain extends BasicModel {
	
	public Domain() {
		super();
	}
	
	public Domain(Map<String, Object> map) {
		super(map);
	}
	
	public String getHostName() { return getAsString("hostname");}
}

