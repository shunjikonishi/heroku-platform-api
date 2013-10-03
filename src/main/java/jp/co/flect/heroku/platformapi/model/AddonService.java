package jp.co.flect.heroku.platformapi.model;

import java.util.Map;
import java.util.Date;

public class AddonService extends BasicModel {
	
	public AddonService() {
		super();
	}
	
	public AddonService(Map<String, Object> map) {
		super(map);
	}
	
	public String getName() { return getAsString("name");}
	
}

